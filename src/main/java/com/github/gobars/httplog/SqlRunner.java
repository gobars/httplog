package com.github.gobars.httplog;

import java.sql.*;
import java.util.*;
import lombok.Cleanup;
import lombok.Value;
import lombok.val;

/**
 * SQL执行器.
 *
 * <p>from
 * https://github.com/mybatis/mybatis-3/blob/master/src/main/java/org/apache/ibatis/jdbc/SqlRunner.java
 */
@Value
public class SqlRunner {
  public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 1001;

  Connection cnn;
  boolean useGeneratedKeySupport;

  /**
   * Executes a SELECT statement that returns one row.
   *
   * @param sql The SQL
   * @param args The arguments to be set on the statement.
   * @return The row expected.
   * @throws SQLException If less or more than one row is returned
   */
  public Map<String, String> selectOne(String sql, Object... args) throws SQLException {
    @Cleanup val ps = cnn.prepareStatement(sql);
    setParameters(ps, args);
    val rs = ps.executeQuery();
    val results = getResults(rs, 2);

    if (results.size() != 1) {
      throw new SQLException("Statement returned more results where exactly one (1) was expected.");
    }

    return results.get(0);
  }

  /**
   * Executes a SELECT statement that returns multiple rows.
   *
   * @param sql The SQL
   * @param args The arguments to be set on the statement.
   * @return The list of rows expected.
   * @throws SQLException If statement preparation or execution fails
   */
  public List<Map<String, String>> selectAll(String sql, Object... args) throws SQLException {
    @Cleanup val ps = cnn.prepareStatement(sql);
    setParameters(ps, args);
    val rs = ps.executeQuery();
    return getResults(rs, -1);
  }

  /**
   * Executes an INSERT statement.
   *
   * @param sql The SQL
   * @param args The arguments to be set on the statement.
   * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
   * @throws SQLException If statement preparation or execution fails
   */
  public int insert(String sql, Object... args) throws SQLException {
    @Cleanup val ps = createPs(sql);

    setParameters(ps, args);
    ps.executeUpdate();

    if (useGeneratedKeySupport) {
      return parseGeneratedKey(ps);
    }

    return NO_GENERATED_KEY;
  }

  private PreparedStatement createPs(String sql) throws SQLException {
    if (useGeneratedKeySupport) {
      return cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }

    return cnn.prepareStatement(sql);
  }

  public int parseGeneratedKey(PreparedStatement ps) throws SQLException {
    val keys = getResults(ps.getGeneratedKeys(), 1);
    if (keys.isEmpty()) {
      return -1;
    }

    Iterator<String> i = keys.get(0).values().iterator();
    if (!i.hasNext()) {
      return -1;
    }

    String genkey = i.next();
    if (genkey == null) {
      return -1;
    }

    try {
      return Integer.parseInt(genkey);
    } catch (NumberFormatException e) {
      // ignore, no numeric key support
    }

    return -1;
  }

  public void setParameters(PreparedStatement ps, Object... args) throws SQLException {
    for (int i = 0, n = args.length; i < n; i++) {
      Object arg = args[i];
      ps.setObject(i + 1, arg);
    }
  }

  public List<Map<String, String>> getResults(ResultSet resultSet, int limit) throws SQLException {
    @Cleanup val rs = resultSet;

    List<String> cols = new ArrayList<>();
    val md = rs.getMetaData();
    for (int i = 0, n = md.getColumnCount(); i < n; i++) {
      cols.add(md.getColumnLabel(i + 1));
    }

    List<Map<String, String>> list = new ArrayList<>();
    int rows = 0;

    for (; rs.next() && (limit <= 0 || rows < limit); rows++) {
      Map<String, String> row = new HashMap<>();
      for (int i = 0, n = cols.size(); i < n; i++) {
        String name = cols.get(i).toLowerCase(Locale.ENGLISH);
        String value = rs.getString(i + 1);
        row.put(name, value);
      }
      list.add(row);
    }

    return list;
  }
}
