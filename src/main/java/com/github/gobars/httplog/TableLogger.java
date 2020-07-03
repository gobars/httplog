package com.github.gobars.httplog;

import com.github.gobars.id.db.SqlRunner;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * 在数据库表中记录日志
 *
 * @author bingoobjca
 */
@Value
@Slf4j
public class TableLogger {
  String sql;
  List<ColValueGetter> valueGetters;

  static TableLogger create(String table, List<TableCol> tableCols) {
    StringBuilder insertSql = new StringBuilder("insert into ").append(table).append("(");
    val insertValueGetters = new ArrayList<ColValueGetter>(10);
    val columnNames = new ArrayList<String>(10);
    val insertMarks = new ArrayList<String>(10);

    for (val tableCol : tableCols) {
      val valueGetter = tableCol.getValueGetter();
      if (valueGetter == null) {
        continue;
      }

      columnNames.add(tableCol.getName());
      insertMarks.add("?");
      insertValueGetters.add(valueGetter);
    }

    insertSql
        .append(String.join(",", columnNames))
        .append(") values(")
        .append(String.join(",", insertMarks))
        .append(")");

    return new TableLogger(insertSql.toString(), insertValueGetters);
  }

  /**
   * 记录响应日志
   *
   * @param run SqlRunner
   * @param r HttpServletRequest
   * @param p HttpServletResponse
   * @param req Req
   * @param rsp Rsp
   * @param httpLog HttpLogAttr
   */
  public void rsp(
      SqlRunner run,
      HttpServletRequest r,
      HttpServletResponse p,
      Req req,
      Rsp rsp,
      HttpLogAttr httpLog) {
    val params = new ArrayList<>(valueGetters.size());
    for (val colValueGetter : valueGetters) {
      try {
        Object obj = colValueGetter.get(req, rsp, r, p, httpLog);
        params.add(obj);
      } catch (Exception ex) {
        params.add(null);
        log.warn("colValueGetter get error", ex);
      }
    }

    try {
      run.insert(sql, params.toArray(new Object[0]));
    } catch (Exception ex) {
      log.warn("sql:{} with params:{} get error", sql, params, ex);
    }
  }
}
