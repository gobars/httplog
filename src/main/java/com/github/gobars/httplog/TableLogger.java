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
public interface TableLogger {
  static TableLogger create(String table, List<TableCol> tableCols, HttpLogAttr httpLog) {
    if (httpLog.eager()) {
      return createEagerTableLogger(table, tableCols);
    }

    return createNonEagerTableLogger(table, tableCols);
  }

  static TableLogger createNonEagerTableLogger(String table, List<TableCol> tableCols) {
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

    return new NonEagerTableLogger(insertSql.toString(), insertValueGetters);
  }

  static EagerTableLogger createEagerTableLogger(String table, List<TableCol> tableCols) {
    StringBuilder insertSql = new StringBuilder("insert into ").append(table).append("(");
    StringBuilder updateSql = new StringBuilder("update ").append(table).append(" set ");
    val insertValueGetters = new ArrayList<ColValueGetter>(10);
    val updateValueGetters = new ArrayList<ColValueGetter>(10);
    val insertMarks = new ArrayList<String>(10);
    val columnNames = new ArrayList<String>(10);
    val updateSets = new ArrayList<String>(10);

    for (val tableCol : tableCols) {
      val valueGetter = tableCol.getValueGetter();
      if (valueGetter == null) {
        continue;
      }

      if (tableCol.eagerSupport()) {
        columnNames.add(tableCol.getName());
        insertMarks.add("?");
        insertValueGetters.add(valueGetter);
      }

      updateSets.add(tableCol.getName() + "=?");
      updateValueGetters.add(valueGetter);
    }

    insertSql
        .append(String.join(",", columnNames))
        .append(") values(")
        .append(String.join(",", insertMarks))
        .append(")");

    updateValueGetters.add((req, rsp, r, p, hl) -> req.getId());
    updateSql.append(String.join(",", updateSets)).append(" where id = ?");

    return new EagerTableLogger(
        insertSql.toString(), insertValueGetters, updateSql.toString(), updateValueGetters);
  }

  /**
   * 记录请求日志
   *
   * @param run SqlRunner
   * @param r HttpServletRequest
   * @param req Req
   * @param httpLog
   */
  default void req(SqlRunner run, HttpServletRequest r, Req req, HttpLogAttr httpLog) {}

  /**
   * 记录响应日志
   *
   * @param run SqlRunner
   * @param r HttpServletRequest
   * @param p HttpServletResponse
   * @param req Req
   * @param rsp Rsp
   * @param httpLog
   */
  void rsp(
      SqlRunner run,
      HttpServletRequest r,
      HttpServletResponse p,
      Req req,
      Rsp rsp,
      HttpLogAttr httpLog);

  /**
   * 两阶段记录（req和rsp).
   *
   * @author bingoobjca
   */
  @Slf4j
  @Value
  class EagerTableLogger implements TableLogger {
    String insertSql;
    List<ColValueGetter> insertValueGetters;
    String updateSql;
    List<ColValueGetter> updateValueGetters;

    @Override
    public void req(SqlRunner run, HttpServletRequest r, Req req, HttpLogAttr httpLog) {
      val params = new ArrayList<>(insertValueGetters.size());
      for (val colValueGetter : insertValueGetters) {
        try {
          Object obj = colValueGetter.get(req, null, r, null, httpLog);
          params.add(obj);
        } catch (Exception ex) {
          params.add(null);
          log.warn("colValueGetter get error", ex);
        }
      }
      try {
        run.insert(insertSql, params.toArray(new Object[0]));
      } catch (Exception ex) {
        log.warn("sql:{} with params:{} get error", insertSql, params, ex);
      }
    }

    @Override
    public void rsp(
        SqlRunner run,
        HttpServletRequest r,
        HttpServletResponse p,
        Req req,
        Rsp rsp,
        HttpLogAttr httpLog) {
      NonEagerTableLogger.log(updateSql, updateValueGetters, run, r, p, req, rsp, httpLog);
    }
  }

  /**
   * 一把头统一在响应时记录
   *
   * @author bingoobjca
   */
  @Value
  @Slf4j
  class NonEagerTableLogger implements TableLogger {
    String sql;
    List<ColValueGetter> valueGetters;

    static void log(
        String sql,
        List<ColValueGetter> valueGetters,
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

    @Override
    public void rsp(
        SqlRunner run,
        HttpServletRequest r,
        HttpServletResponse p,
        Req req,
        Rsp rsp,
        HttpLogAttr httpLog) {
      log(sql, valueGetters, run, r, p, req, rsp, httpLog);
    }
  }
}
