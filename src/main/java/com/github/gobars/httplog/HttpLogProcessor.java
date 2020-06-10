package com.github.gobars.httplog;

import com.github.gobars.id.conf.ConnGetter;
import com.github.gobars.id.db.SqlRunner;
import com.github.gobars.id.util.DbType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * HttpLog日志处理器类.
 *
 * @author bingoobjca
 */
@Slf4j
public class HttpLogProcessor {
  private final HttpLog httpLog;
  private final boolean eager;
  private final Map<String, TableLogger> sqlGenerators;
  private final ConnGetter connGetter;

  public HttpLogProcessor(
      HttpLog httpLog, Map<String, TableLogger> sqlGenerators, ConnGetter connGetter) {
    this.httpLog = httpLog;
    this.eager = httpLog.eager();
    this.sqlGenerators = sqlGenerators;
    this.connGetter = connGetter;
  }

  @SneakyThrows
  public static HttpLogProcessor create(HttpLog httpLog, ConnGetter connGetter) {
    val ms =
        "select column_name, column_comment, data_type,"
            + " character_maximum_length max_length, ordinal_position column_id"
            + " from information_schema.columns"
            + " where table_schema = database()"
            + "  and table_name = ?";
    val os =
        "select tc.column_id,"
            + "       tc.COLUMN_NAME column_name,"
            + "       tc.DATA_TYPE   data_type,"
            + "       tc.DATA_LENGTH max_length,"
            + "       cc.COMMENTS    column_comment"
            + " from user_col_comments cc"
            + "   inner join user_tab_cols tc"
            + "   on (cc.table_name = tc.table_name and cc.column_name = tc.column_name)"
            + " where cc.table_name = upper(?)";
    @Cleanup val conn = connGetter.getConn();

    DbType dbType = DbType.getDbType(conn);
    val s = dbType == DbType.MYSQL ? ms : os;
    val runner = new SqlRunner(conn, false);

    val sqlGenerators = new HashMap<String, TableLogger>(httpLog.tables().length);
    val fixes = Str.parseMap(httpLog.fix(), ",", ":");

    for (val table : httpLog.tables()) {
      val maps = runner.selectAll(s, table);
      val tableCols = new ArrayList<TableCol>(maps.size());

      for (val m : maps) {
        val tableCol = new TableCol();
        tableCols.add(tableCol);

        setStr(m, "column_name", tableCol::setName);
        setStr(m, "column_comment", tableCol::setComment);
        setStr(m, "data_type", tableCol::setDataType);
        setInt(m, "max_length", tableCol::setMaxLen);
        setInt(m, "column_id", tableCol::setSeq);

        tableCol.parseComment(fixes, httpLog);
      }

      log.info("tableCols: {}", tableCols);

      sqlGenerators.put(table, TableLogger.create(table, tableCols, httpLog));
    }

    return new HttpLogProcessor(httpLog, sqlGenerators, connGetter);
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

  @SneakyThrows
  public void logReq(HttpServletRequest r, Req req, HttpLog httpLog) {
    if (!this.eager) {
      return;
    }

    log.info("eager req:{}", req);

    @Cleanup val conn = connGetter.getConn();
    for (val table : httpLog.tables()) {
      val runner = new SqlRunner(conn, false);
      val sqlGenerator = sqlGenerators.get(table);

      sqlGenerator.req(runner, r, req, httpLog);
    }
  }

  @SneakyThrows
  public void complete(HttpServletRequest r, HttpServletResponse p, Rsp rsp, HttpLog httpLog) {
    Req req = (Req) r.getAttribute(HttpLogFilter.HTTPLOG_REQ);
    log.info("eager complete:{}", req);

    @Cleanup val conn = connGetter.getConn();
    for (val table : this.httpLog.tables()) {
      val runner = new SqlRunner(conn, false);
      val sqlGenerator = sqlGenerators.get(table);

      sqlGenerator.rsp(runner, r, p, req, rsp, httpLog);
    }
  }
}
