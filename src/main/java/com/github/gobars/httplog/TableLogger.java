package com.github.gobars.httplog;

import com.github.gobars.httplog.snack.Onode;
import com.github.ksuid.Ksuid;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

  static TableLogger create(String table, List<TableCol> tableCols, DbType dbType) {
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

    if (dbType == DbType.MYSQL) {
      insertSql.append(String.join(",", columnNames));
    } else {
      insertSql.append("\"").append(String.join("\",\"", columnNames)).append("\"");
    }
    insertSql.append(") values(").append(String.join(",", insertMarks)).append(")");

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
   * @param table
   * @param ctx ColValueGetterContext
   */
  public LogPrepared prepareLog(String table, ColValueGetterCtx ctx) {
    ArrayList<Object> params = new ArrayList<>(valueGetters.size());
    // for id update when duplicate error detected.
    List<Integer> idPositions = new ArrayList<>();
    for (val colValueGetter : valueGetters) {
      Object obj = TableValueFilterUtil.filter(table, ctx, getVal(ctx, colValueGetter));

      if (ctx.req().getId().equals(obj)) {
        idPositions.add(params.size());
      }

      params.add(convertSetObject(obj));
    }

    log.debug("SQL {} with args {}", sql, params);
    return new LogPrepared(sql, params, idPositions);
  }

  private Object convertSetObject(Object o) {
    if (o == null) {
      return null;
    }

    if (o instanceof String
        || o instanceof Number
        || o instanceof byte[]
        || o instanceof Date
        || o instanceof Boolean
        || o instanceof InputStream
        || o instanceof LocalDate
        || o instanceof LocalDateTime
        || o instanceof LocalTime) {
      return o;
    }

    try {
      return Onode.load(o).toJson();
    } catch (Exception e) {
      // Ignore
    }

    try {
      return o.toString();
    } catch (Exception e) {
      // Ignore
    }

    return null;
  }

  @Nullable
  public Object getVal(ColValueGetterCtx ctx, ColValueGetter colValueGetter) {
    try {
      return colValueGetter.get(ctx);
    } catch (Exception ex) {
      log.warn("colValueGetter get error", ex);
    }

    return null;
  }

  /**
   * 记录响应日志
   *
   * @param conn
   * @param run SqlRunner
   * @param logPrepared LogPrepared
   */
  public static void rsp(Connection conn, SqlRunner run, LogPrepared logPrepared) {
    for (int i = 0; i < MAX_RETRY; i++) {
      if (rspInternal(conn, run, logPrepared)) {
        return;
      }
    }
  }

  private static final int MAX_RETRY = 3;

  private static boolean rspInternal(Connection conn, SqlRunner run, LogPrepared logPrepared) {
    try {
      run.insert(logPrepared.getSql(), logPrepared.getParams().toArray(new Object[0]));
    } catch (Exception ex) {
      if (isDuplicateKeyException(conn, ex)) {
        log.warn("logPrepared {} got duplicate key, retry", logPrepared, ex);
        String newid = Ksuid.newKsuid().toString();

        for (int idPosition : logPrepared.getIdPositions()) {
          logPrepared.getParams().set(idPosition, newid);
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
   * <p>Acoording to Google, "23000" indicates a unique constraint violation in at least MySQL,
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
  public static boolean isDuplicateKeyException(Connection conn, Exception e) {
    if (!(e instanceof SQLException)) {
      return false;
    }

    SQLException se = (SQLException) e;

    switch (DbType.getDbType(conn)) {
      case MYSQL:
        return se.getErrorCode() == 1062;
      case ORACLE:
        return se.getErrorCode() == 1;
      default:
        return se.getSQLState().equals("23000");
    }
  }
}
