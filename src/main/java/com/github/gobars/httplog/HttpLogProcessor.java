package com.github.gobars.httplog;

import com.github.gobars.id.conf.ConnGetter;
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
    val s =
        "select column_name, column_comment, data_type, column_type, character_maximum_length, ordinal_position"
            + " from information_schema.columns"
            + " where table_schema = database()"
            + "  and table_name = ?";
    @Cleanup val conn = connGetter.getConn();
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
        setStr(m, "column_type", tableCol::setType);
        setInt(m, "character_maximum_length", tableCol::setMaxLen);
        setInt(m, "ordinal_position", tableCol::setSeq);

        tableCol.parseComment(fixes);
      }

      log.info("tableCols: {}", tableCols);

      sqlGenerators.put(table, TableLogger.create(table, tableCols, httpLog));
    }

    return new HttpLogProcessor(httpLog, sqlGenerators, connGetter);
  }

  private static Map<String, String> parseHttpLogFix(String s) {
    return null;
  }

  private static void setStr(Map<String, String> m, String key, Consumer<String> consumer) {
    String v = m.get(key);
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
  public void logReq(HttpServletRequest r, Req req) {
    if (!this.eager) {
      return;
    }

    log.info("eager req:{}", req);

    @Cleanup val conn = connGetter.getConn();
    for (val table : httpLog.tables()) {
      val runner = new SqlRunner(conn, false);
      val sqlGenerator = sqlGenerators.get(table);

      sqlGenerator.req(runner, r, req);
    }
  }

  @SneakyThrows
  public void complete(HttpServletRequest r, HttpServletResponse p, Rsp rsp) {
    Req req = (Req) r.getAttribute(Filter.HTTPLOG_REQ);
    log.info("eager complete:{}", req);

    @Cleanup val conn = connGetter.getConn();
    for (val table : httpLog.tables()) {
      val runner = new SqlRunner(conn, false);
      val sqlGenerator = sqlGenerators.get(table);

      sqlGenerator.rsp(runner, r, p, req, rsp);
    }
  }
}
