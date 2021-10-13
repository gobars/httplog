package com.github.gobars.httplog;

import com.github.gobars.httplog.TableLogger.LogPrepared;
import com.github.gobars.httplog.springconfig.HttpLogYml;
import com.github.gobars.id.conf.ConnGetter;
import com.github.gobars.id.db.SqlRunner;
import com.github.gobars.id.util.DbType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import static java.nio.charset.StandardCharsets.UTF_8;

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

  public static String asString(Resource resource) {
    try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
      return FileCopyUtils.copyToString(reader);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @SneakyThrows
  public static HttpLogProcessor create(
      HttpLogAttr httpLog, ConnGetter connGetter, ApplicationContext appContext) {
    // MYSQL: IS_NULLABLE YES NO
    // ORACLE: NULLABLE Y N
    @Cleanup val conn = connGetter.getConn();

    SchemaSql schemaSql = getSchemaSql(DbType.getDbType(conn));
    val runner = new SqlRunner(conn, false);

    val sqlGenerators = new HashMap<String, TableLogger>(httpLog.tables().length);
    val fixes = Str.parseMap(httpLog.fix(), ",", ":");

    val httpLogYml = getBeanOfType(appContext, HttpLogYml.class);

    for (val table : httpLog.tables()) {
      List<TableCol> tableCols =
          readTableColsSchema(httpLogYml, appContext, conn, schemaSql, runner, fixes, table);
      log.debug("tableCols: {}", tableCols);

      if (CollectionUtils.isEmpty(tableCols)) {
        // 没有从数据库中查到表字段的元信息，尝试从 httplog.yml 中载入
        tableCols = httpLogYml.loadTableColsSchema(table, fixes);
      }

      if (CollectionUtils.isEmpty(tableCols)) {
        throw new RuntimeException("failed to load meta info for table " + table);
      }

      sqlGenerators.put(table, TableLogger.create(table, tableCols, DbType.getDbType(conn)));
    }

    return new HttpLogProcessor(httpLog, sqlGenerators, connGetter, fixes, appContext);
  }

  public static <T> T getBeanOfType(ApplicationContext appContext, Class<T> type) {
    Map<String, T> beans = appContext.getBeansOfType(type);
    if (beans.isEmpty()) {
      return null;
    }

    return beans.entrySet().iterator().next().getValue();
  }

  private static List<TableCol> readTableColsSchema(
      HttpLogYml httpLogYml,
      ApplicationContext appContext,
      Connection conn,
      SchemaSql schemaSql,
      SqlRunner runner,
      Map<String, String> fixes,
      String table) {
    // 如果不是自动读取表元信息的话，直接返回
    if (httpLogYml != null && !httpLogYml.isAutoSchema()) {
      return null;
    }

    try {
      return readTableColsSchemaEx(appContext, conn, schemaSql, runner, fixes, table);
    } catch (SQLException ex) {
      return null;
    }
  }

  private static List<TableCol> readTableColsSchemaEx(
      ApplicationContext appContext,
      Connection conn,
      SchemaSql schemaSql,
      SqlRunner runner,
      Map<String, String> fixes,
      String table)
      throws SQLException {
    val tableCols = new ArrayList<TableCol>();
    Object[] args = schemaSql.args.getArgs(conn.getSchema(), table);
    val maps = runner.selectAll(schemaSql.query, args);
    for (val m : maps) {
      val tableCol = new TableCol();
      tableCols.add(tableCol);

      setStr(m, "column_name", tableCol::setName);
      setStr(m, "column_comment", tableCol::setComment);
      setStr(m, "data_type", tableCol::setDataType);
      setStr(m, "extra", tableCol::setExtra);
      setInt(m, "max_length", tableCol::setMaxLen);
      setBool(m, "nullable", tableCol::setNullable, true);

      tableCol.parseComment(table, appContext, fixes);
    }

    return tableCols;
  }

  static class SchemaSql {
    public String query;
    public SchemaArgs args;

    public SchemaSql(String query, SchemaArgs args) {
      this.query = query;
      this.args = args;
    }
  }

  @FunctionalInterface
  interface SchemaArgs {
    Object[] getArgs(String schema, String table);
  }

  private static SchemaSql getSchemaSql(DbType dbType) {
    switch (dbType) {
      case MYSQL:
        return new SchemaSql(
            asString(new ClassPathResource("schema-mysql.sql")),
            (schema, table) -> new Object[] {table});
      case DM:
        return new SchemaSql(
            asString(new ClassPathResource("schema-dm.sql")),
            (schema, table) -> new Object[] {schema, table, table});
      case ORACLE:
        return new SchemaSql(
            asString(new ClassPathResource("schema-oracle.sql")),
            (schema, table) -> new Object[] {table});
      case SHENTONG:
        return new SchemaSql(
            asString(new ClassPathResource("schema-kingbase.sql")),
            (schema, table) -> new Object[] {table.toUpperCase()});
      case KINGBASE:
        return new SchemaSql(
            asString(new ClassPathResource("schema-kingbase.sql")),
            (schema, table) -> new Object[] {table});
      case POSTGRESQL:
        return new SchemaSql(
            asString(new ClassPathResource("schema-postgre.sql")),
            (schema, table) -> new Object[] {table});
      default:
        throw new RuntimeException("not support db");
    }
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
