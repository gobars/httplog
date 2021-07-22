package com.github.gobars.httplog;

import com.github.gobars.httplog.TableLogger.LogPrepared;
import com.github.gobars.id.conf.ConnGetter;
import com.github.gobars.id.db.SqlRunner;
import com.github.gobars.id.util.DbType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

/**
 * HttpLog日志处理器类.
 *
 * @author bingoobjca
 */
@Slf4j
public class HttpLogProcessor {
  private final HttpLogAttr httpLog;
  private final Map<String, TableLogger> sqlGenerators;
  private final ConnGetter connGetter;
  private final HttpLogPre pre;
  private final HttpLogPost post;
  private final Map<String, String> fixes;
  private final TaskExecutor taskExecutor;

  public HttpLogProcessor(
      HttpLogAttr httpLog,
      Map<String, TableLogger> sqlGenerators,
      ConnGetter connGetter,
      Map<String, String> fixes,
      ApplicationContext appContext) {
    this.httpLog = httpLog;
    this.sqlGenerators = sqlGenerators;
    this.connGetter = connGetter;
    this.pre = new HttpLogPre.HttpLogPreComposite(createExt(httpLog.pre(), appContext));
    this.post = new HttpLogPost.HttpLogPostComposite(createExt(httpLog.post(), appContext));
    this.fixes = fixes;
    this.taskExecutor = createTaskExecutor(httpLog, appContext);
  }

  @SneakyThrows
  public static HttpLogProcessor create(
      HttpLogAttr httpLog, ConnGetter connGetter, ApplicationContext appContext) {
    // MYSQL: IS_NULLABLE YES NO
    // ORACLE: NULLABLE Y N
    val ms =
        "select column_name, column_comment, data_type, extra, is_nullable nullable, "
            + " character_maximum_length max_length, ordinal_position column_id"
            + " from information_schema.columns"
            + " where table_schema = database()"
            + "  and table_name = ?";
    val dmos =
        "select tc.column_id,"
            + "       tc.COLUMN_NAME column_name,"
            + "       tc.DATA_TYPE   data_type,"
            + "       tc.DATA_LENGTH max_length,"
            + "       tc.NULLABLE     nullable,"
            + "       cc.COMMENTS    column_comment, it.INFO2 extra"
            + " from user_col_comments cc"
            + "   inner join user_tab_cols tc"
            + "   on (cc.table_name = tc.table_name and cc.column_name = tc.column_name)"
            + "      left JOIN (SELECT * FROM syscolumns t"
            + "                  WHERE id ="
            + "                    (SELECT object_id FROM dba_objects t "
            + "                         WHERE 1=1 "
                                     + "AND t.owner = ? "
            + "                      AND object_type = 'TABLE' "
            + "                      AND t.object_name = ?)"
            + ") it on (it.NAME = tc.COLUMN_NAME)"
            + " where cc.table_name = upper(?)";
    val os =
        "select tc.column_id,"
            + "       tc.COLUMN_NAME column_name,"
            + "       tc.DATA_TYPE   data_type,"
            + "       tc.DATA_LENGTH max_length,"
            + "       tc.NULLABLE     nullable,"
            + "       cc.COMMENTS    column_comment"
            + " from user_col_comments cc"
            + "   inner join user_tab_cols tc"
            + "   on (cc.table_name = tc.table_name and cc.column_name = tc.column_name)"
            + " where cc.table_name = upper(?)";
    val ks =
        "  select column_id,column_name,  max_length, nullable,ad.adsrc extra, "
            + "column_comment  from (select  a.attnum column_id, a.attname column_name, "
            + "a.atttypmod max_length, a.attnotnull nullable, c.oid oid,  a.attnum attnum, d.description column_comment "
            + "from sys_class c, sys_attribute a "
            + "left join sys_description d on d.objoid=a.attrelid  and d.objsubid=a.attnum where c.oid=a.attrelid "
            + "and c.relname = ?  and a.attnum>0) b  left join sys_attrdef ad on b.attnum = ad.adnum and ad.adrelid=b.oid";
    val ps =
        "  select column_id,column_name,  max_length, nullable,ad.adsrc extra, "
            + "column_comment  from (select  a.attnum column_id, a.attname column_name, "
            + "a.atttypmod max_length, a.attnotnull nullable, c.oid oid,  a.attnum attnum, d.description column_comment "
            + "from pg_class c, pg_attribute a "
            + "left join pg_description d on d.objoid=a.attrelid  and d.objsubid=a.attnum where c.oid=a.attrelid "
            + "and c.relname = ?  and a.attnum>0) b  left join pg_attrdef ad on b.attnum = ad.adnum and ad.adrelid=b.oid";
    @Cleanup val conn = connGetter.getConn();

    DbType dbType = DbType.getDbType(conn);
    String s;
    switch (dbType) {
      case MYSQL:
        s = ms;
        break;
      case DM:
        s = dmos;
        break;
      case ORACLE:
        s = os;
        break;
      case KINGBASE:
        s = ks;
        break;
      case POSTGRESQL:
        s = ps;
        break;
      default:
        throw new RuntimeException("not support db");
    }
    val runner = new SqlRunner(conn, false);

    val sqlGenerators = new HashMap<String, TableLogger>(httpLog.tables().length);
    val fixes = Str.parseMap(httpLog.fix(), ",", ":");

    for (val table : httpLog.tables()){
      val maps = dbType == DbType.DM ?
          runner.selectAll(s, conn.getSchema(), table, table)
          : runner.selectAll(s, table);
      val tableCols = new ArrayList<TableCol>(maps.size());

      for (val m : maps) {
        val tableCol = new TableCol();
        tableCols.add(tableCol);

        setStr(m, "column_name", tableCol::setName);
        setStr(m, "column_comment", tableCol::setComment);
        setStr(m, "data_type", tableCol::setDataType);
        setStr(m, "extra", tableCol::setExtra);
        setInt(m, "max_length", tableCol::setMaxLen);
        setInt(m, "column_id", tableCol::setSeq);
        setBool(m, "nullable", tableCol::setNullable, true);

        tableCol.parseComment(table, appContext, fixes);
      }

      log.debug("tableCols: {}", tableCols);

      sqlGenerators.put(table, TableLogger.create(table, tableCols, dbType));
    }

    return new HttpLogProcessor(httpLog, sqlGenerators, connGetter, fixes, appContext);
  }

  private static void setStr(Map<String, String> m, String key, Consumer<String> consumer) {
    String v = m.get(key);
    if (v == null) {
      v = m.get(key.toUpperCase());
    }

    if (v != null) {
      consumer.accept(v);
    }
  }

  private static void setBool(
      Map<String, String> m, String key, Consumer<Boolean> consumer, boolean defaultValue) {
    String v = m.get(key);
    if (v == null) {
      v = m.get(key.toUpperCase());
    }

    if (v != null) {
      consumer.accept(v.toLowerCase().startsWith("y"));
    } else {
      consumer.accept(defaultValue);
    }
  }

  private static void setInt(Map<String, String> m, String key, IntConsumer consumer) {
    String v = m.get(key);
    if (v == null) {
      return;
    }
    try {
      consumer.accept(Integer.parseInt(v));
    } catch (NumberFormatException ex) {
      // ignore
    }
  }

  private TaskExecutor createTaskExecutor(HttpLogAttr httpLog, ApplicationContext appContext) {
    if (httpLog.sync()) {
      return Runnable::run;
    }

    try {
      return appContext.getBean(TaskExecutor.class);
    } catch (Exception ex) {
      log.warn("failed to get TaskExecutor bean");
    }

    return Runnable::run;
  }

  private <T> List<T> createExt(Class<? extends T>[] exts, ApplicationContext appContext) {
    val composite = new ArrayList<T>();

    if (exts != null) {
      for (val ext : exts) {
        val p = create(appContext, ext);
        if (p != null) {
          composite.add(p);
        }
      }
    }

    return composite;
  }

  private <T> T create(ApplicationContext appContext, Class<? extends T> ext) {
    if (appContext != null) {
      val beans = appContext.getBeansOfType(ext);
      if (beans.size() == 1) {
        return beans.entrySet().iterator().next().getValue();
      }
    }

    try {
      return ext.getConstructor().newInstance();
    } catch (Exception ex) {
      log.warn("failed to newInstance of {}", ext, ex);
    }

    return null;
  }

  @SneakyThrows
  public void logReq(HttpServletRequest r, Req req) {
    req.setPres(createPre(r, req, httpLog));
  }

  @SneakyThrows
  public void complete(HttpServletRequest r, HttpServletResponse p, Req req, Rsp rsp) {
    List<LogPrepared> prepareds = prepareLogs(r, p, req, rsp);

    taskExecutor.execute(() -> run(prepareds));
  }

  @SneakyThrows
  public void run(List<LogPrepared> prepareds) {
    @Cleanup val conn = connGetter.getConn();

    val run = new SqlRunner(conn, false);
    for (LogPrepared prepared : prepareds) {
      TableLogger.rsp(conn, run, prepared);
    }
  }

  public List<LogPrepared> prepareLogs(
      HttpServletRequest r, HttpServletResponse p, Req req, Rsp rsp) {
    req.setAbbrevMaxSize(httpLog.abbrevMaxSize());
    rsp.setAbbrevMaxSize(httpLog.abbrevMaxSize());

    log.info("req: {}", req);
    log.info("rsp: {}", rsp);
    HttpLogCustom custom = (HttpLogCustom) r.getAttribute(Const.CUSTOM);
    log.info("custom: {}", custom.getMap());

    List<LogPrepared> prepareds = new ArrayList<>();

    try {
      rsp.setPosts(createPost(req, rsp, r, p, httpLog));

      for (val table : httpLog.tables()) {
        val ctx = new ColValueGetterCtx().r(r).p(p).req(req).rsp(rsp).hl(httpLog);
        LogPrepared lp = sqlGenerators.get(table).prepareLog(table, ctx);
        prepareds.add(lp);
      }
    } catch (Exception ex) {
      log.warn("failed to log req:{} rsp:{} for httpLog:{}", req, rsp, httpLog, ex);
    }

    ArrayList<HttpLogFork> forks = custom.getForks();

    if (forks.isEmpty()) {
      return prepareds;
    }

    HttpLogInterceptor hli = (HttpLogInterceptor) r.getAttribute(Const.INTERCEPTOR);

    for (HttpLogFork f : forks) {
      HttpLogProcessor ps = hli.cacheGet(f.getAttr());
      for (val table : f.getAttr().tables()) {
        val ctx = new ColValueGetterCtx().r(r).p(p).req(req).rsp(rsp).hl(httpLog).fork(f);
        prepareds.add(ps.sqlGenerators.get(table).prepareLog(table, ctx));
      }
    }

    return prepareds;
  }

  private Map<String, String> createPre(HttpServletRequest r, Req req, HttpLogAttr hl) {
    val m = new HashMap<String, String>(10);
    if (this.pre == null) {
      return m;
    }

    try {
      return pre.create(r, req, hl, fixes);
    } catch (Exception ex) {
      log.warn("pre {} create error", pre, ex);
    }

    return m;
  }

  private Map<String, String> createPost(
      Req req, Rsp rsp, HttpServletRequest r, HttpServletResponse p, HttpLogAttr hl) {
    val m = new HashMap<String, String>(10);
    if (this.post == null) {
      return m;
    }

    try {
      return post.create(r, p, req, rsp, hl, fixes);
    } catch (Exception ex) {
      log.warn("pre {} create error", pre, ex);
    }

    return m;
  }
}
