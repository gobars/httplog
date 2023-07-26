package com.github.gobars.httplog;

import lombok.Cleanup;
import lombok.val;

import java.sql.*;
import java.util.*;

/**
 * SQL执行器.
 *
 * <p>from
 * https://github.com/mybatis/mybatis-3/blob/master/src/main/java/org/apache/ibatis/jdbc/SqlRunner.java
 */
public class SqlRunner {
    public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 1001;

    Connection cnn;
    boolean useGeneratedKeySupport;

    public SqlRunner(Connection cnn) {
        this(cnn, false);
    }

    public SqlRunner(Connection cnn, boolean useGeneratedKeySupport) {
        this.cnn = cnn;
        this.useGeneratedKeySupport = useGeneratedKeySupport;
    }

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
        @Cleanup val rs = ps.executeQuery();
        val results = getResults(rs, 1);

        if (results.size() != 1) {
            throw new SQLException("Statement returned more results where exactly one (1) was expected.");
        }

        return results.get(0);
    }

    /**
     * Executes a SELECT statement that returns the first column's long value.
     *
     * @param sql The SQL
     * @param args The arguments to be set on the statement.
     * @return the first column's long value.
     * @throws SQLException If less or more than one row is returned
     */
    public long selectLong(String sql, Object... args) throws SQLException {
        @Cleanup val ps = cnn.prepareStatement(sql);
        setParameters(ps, args);
        @Cleanup val rs = ps.executeQuery();
        return getLong(rs);
    }

    /**
     * Executes a SELECT statement that returns the first column's long value.
     *
     * @param limit limit rows to scan.
     * @param scanner the user defined scanner.
     * @param sql The SQL.
     * @param args The arguments to be set on the statement.
     * @throws SQLException If less or more than one row is returned
     */
    public void select(int limit, RowScanner scanner, String sql, Object... args)
            throws SQLException {
        @Cleanup val ps = cnn.prepareStatement(sql);
        setParameters(ps, args);
        @Cleanup val rs = ps.executeQuery();
        getObjects(rs, limit, scanner);
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
        @Cleanup val rs = ps.executeQuery();
        return getResults(rs, -1);
    }

    /**
     * Executes an INSERT statement.
     *
     * @param sql The update/insert SQL
     * @param args The arguments to be set on the statement.
     * @return The number of rows impacted or BATCHED_RESULTS if the statements are being batched.
     * @throws SQLException If statement preparation or execution fails
     */
    public int insert(String sql, Object... args) throws SQLException {
        @Cleanup val ps = prepareStatement(cnn, sql);

        setParameters(ps, args);
        ps.executeUpdate();

        if (useGeneratedKeySupport) {
            return parseGeneratedKey(ps);
        }

        return NO_GENERATED_KEY;
    }

    /**
     * Executes an UPDATE statement.
     *
     * @param sql The updatet SQL
     * @param args The arguments to be set on the statement.
     * @return The number of rows effected.
     * @throws SQLException If statement preparation or execution fails
     */
    public int update(String sql, Object... args) throws SQLException {
        @Cleanup val ps = prepareStatement(cnn, sql);
        setParameters(ps, args);
        return ps.executeUpdate();
    }

    /**
     * https://blog.csdn.net/xpsharp/article/details/7678028 ORACLE JDBC的getGeneratedKeys 对于JDBC 3.0，
     * 使用statement.getGeneratedKeys()可以返回刚刚插入的记录的自动增长的ID值。对于ORACLE，
     * 一般是定义一个序列，然后利用序列的nextval来自动给列分配ID值。 但是很多人发现，在利用ORACLE JDBC驱动编写的时候，往往会失败。
     * 显示“java.sql.SQLException: Unsupported feature”。
     *
     * <p>其实，对于ORACLE JDBC，只有在10.2.0.1.0版本后的JDBC才支持getGeneratedKeys特性。而且如果使用下列代码： <code>
     * String sql = "INSERT INTO FOO (NAME) VALUES ('BAR')";
     * Statement stmt = connection.createStatement();
     * stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
     * ResultSet rs = stmt.getGeneratedKeys();
     * oracle-ddl.sql.ROWID rid = (oracle-ddl.sql.ROWID) rs.getObject(1); //getLong and getInt fail
     *
     * // The following fail
     * // long l = rid.longValue();
     * // int i = rid.intValue();
     *
     * String s = rid.stringValue(); // s equals "AAAXcTAAEAAADXYAAB"
     * </code> 返回的将是ROWID值。可以使用下列代码： <code>
     * String sql = "INSERT INTO ORDERS (ORDER_ID, CUSTOMER_ID) VALUES (ORDER_ID_SEQ.NEXTVAL, ?)";
     * String generatedColumns[] = {"ORDER_ID"};
     * PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns);
     * pstmt.setLong(1, customerId);
     * pstmt.executeUpdate();
     * ResultSet rs = pstmt.getGeneratedKeys();
     * rs.next();
     * // The generated order id
     * long orderId = rs.getLong(1);
     * </code> 能得到正确的ID值。注意，其中generatedColumns[]表示从哪个列来获取新的ID值。我们也可以使用： <code>
     * int a[]={1};
     * PreparedStatement pstmt = conn.prepareStatement(sql, a);
     * </code> 来表示第1列是KEY列，我们要获取第1列的新插入的值。
     *
     * <p>目前（20071219）ORACLE
     * JDBC最新的是11g1，推荐使用。http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/htdocs/jdbc_11...
     *
     * <p>另外，Jdeveloper 10.1.3.3里面携带的JDBC驱动也不支持getGeneratedKeys。需要进行更新。
     *
     * <p>当然，也可以使用第三方的JDBC驱动，如i-net software的Oranxo驱动，支持getGeneratedKeys，而且驱动程序的体积更小。
     *
     * @param cnn 数据库连接
     * @param sql SQL语句
     * @return PreparedStatement
     * @throws SQLException SQLException
     */
    private PreparedStatement prepareStatement(Connection cnn, String sql) throws SQLException {
        if (!useGeneratedKeySupport) {
            return cnn.prepareStatement(sql);
        }

        switch (DbType.getDbType(cnn)) {
            case MYSQL:
            case KINGBASE:
            case POSTGRESQL:
            case SHENTONG:
                return cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            case DM:
            case ORACLE:
                return cnn.prepareStatement(sql, new int[] {1});
            default:
                throw new SQLException("unsupported db");
        }
    }

    private int parseGeneratedKey(PreparedStatement ps) throws SQLException {
        @Cleanup val rs = ps.getGeneratedKeys();
        val keys = getResults(rs, 1);
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

    private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0, n = args.length; i < n; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    public List<Map<String, String>> getResults(ResultSet rs, int limit) throws SQLException {
        val cols = new ArrayList<String>(10);
        val md = rs.getMetaData();
        for (int i = 0, n = md.getColumnCount(); i < n; i++) {
            cols.add(md.getColumnLabel(i + 1));
        }

        val list = new ArrayList<Map<String, String>>();

        for (int rows = 0; rs.next() && (limit <= 0 || rows < limit); rows++) {
            val row = new HashMap<String, String>();
            for (int i = 0, n = cols.size(); i < n; i++) {
                String name = cols.get(i).toLowerCase(Locale.ENGLISH);
                String value = rs.getString(i + 1);
                row.put(name, value);
            }
            list.add(row);
        }

        return list;
    }

    public long getLong(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rs.getLong(1);
        }

        throw new NoRowsFoundException();
    }

    public interface RowScanner {
        boolean scanRow(int rowIndex, ResultSet rs) throws SQLException;
    }

    public void getObjects(ResultSet rs, int limit, RowScanner scanner) throws SQLException {
        for (int i = 0; rs.next() && (limit <= 0 || i < limit); i++) {
            if (!scanner.scanRow(i, rs)) {
                break;
            }
        }
    }

    public static class NoRowsFoundException extends SQLException {}
}