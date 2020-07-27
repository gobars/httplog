package com.github.gobars.httplog;

import com.github.gobars.id.Id;
import com.github.gobars.id.db.SqlRunner;
import java.sql.SQLException;
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

  @Value
  public static class LogPrepared {
    String sql;
    ArrayList<Object> params;
    List<Integer> idPositions;
  }

  /**
   * 准备记录响应日志
   *
   * @param r HttpServletRequest
   * @param p HttpServletResponse
   * @param req Req
   * @param rsp Rsp
   * @param httpLog HttpLogAttr
   */
  public LogPrepared prepareLog(
      HttpServletRequest r, HttpServletResponse p, Req req, Rsp rsp, HttpLogAttr httpLog) {
    ArrayList<Object> params = new ArrayList<>(valueGetters.size());
    List<Integer> idPositions = new ArrayList<>();
    for (val colValueGetter : valueGetters) {
      Object obj;
      try {
        obj = colValueGetter.get(req, rsp, r, p, httpLog);
      } catch (Exception ex) {
        obj = null;
        log.warn("colValueGetter get error", ex);
      }

      Long id = req.getId();
      if (id.equals(obj)) {
        idPositions.add(params.size());
      }

      params.add(obj);
    }

    log.debug("SQL {} with args {}", sql, params);
    return new LogPrepared(sql, params, idPositions);
  }

  /**
   * 记录响应日志
   *
   * @param run SqlRunner
   * @param logPrepared LogPrepared
   */
  public static void rsp(SqlRunner run, LogPrepared logPrepared) {
    for (int i = 0; i < MAX_RETRY; i++) {
      if (rspInternal(run, logPrepared)) {
        return;
      }
    }
  }

  private static final int MAX_RETRY = 10;

  private static boolean rspInternal(SqlRunner run, LogPrepared logPrepared) {
    try {
      run.insert(logPrepared.getSql(), logPrepared.getParams().toArray(new Object[0]));
    } catch (Exception ex) {
      if (isConstraintViolation(ex)) {
        log.warn("logPrepared {} got duplicate key, retry", logPrepared, ex);
        long newID = Id.next();

        for (int idPosition : logPrepared.getIdPositions()) {
          logPrepared.getParams().set(idPosition, newID);
        }

        return false;
      }
      log.warn("logPrepared {} get error", logPrepared, ex);
    }

    return true;
  }

  /**
   * This is exactly what SQLException.getSQLState() is for.
   *
   * <p>Acoording to Google, "23000" indicates a Junique constraint violation in at least MySQL,
   * PostgreSQL, and Oracle.
   *
   * <p>https://stackoverflow.com/a/727589
   *
   * <p>https://github.com/spring-projects/spring-framework/blob/master/spring-jdbc/src/main/resources/org/springframework/jdbc/support/sql-error-codes.xml
   *
   * <p>MySQL: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '511122530304' for
   * key 'PRIMARY'
   *
   * <p>Oracle: java.sql.SQLException: ORA-00001: 违反唯一约束条件 (SYSTEM.SYS_C006988)
   *
   * <p>Determine if SQLException#getSQLState() of the catched SQLException
   *
   * <p>starts with 23 which is a constraint violation as per the SQL specification.
   *
   * <p>It can namely be caused by more factors than "just" a constraint violation.
   *
   * <p>You should not amend every SQLException as a constraint violation.
   *
   * <p>ORACLE:
   *
   * <p>[2017-03-26 15:13:07] [23000][1] ORA-00001: 违反唯一约束条件 (SYSTEM.SYS_C007109)
   *
   * <p>MySQL:
   *
   * <p>[2017-03-26 15:17:27] [23000][1062] Duplicate entry '1' for key 'PRIMARY'
   *
   * <p>H2:
   *
   * <p>[2017-03-26 15:19:52] [23505][23505] Unique index or primary key violation:
   *
   * <p>"PRIMARY KEY ON PUBLIC.TT(A)"; SQL statement:
   */
  public static boolean isConstraintViolation(Exception e) {
    return e instanceof SQLException && ((SQLException) e).getSQLState().startsWith("23");
  }
}
