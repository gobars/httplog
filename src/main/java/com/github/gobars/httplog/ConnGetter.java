package com.github.gobars.httplog;


import lombok.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnGetter {
    /**
     * Attempts to establish a connection with the data source that this {@code DataSource} object
     * represents.
     *
     * @return a connection to the data source
     * @exception SQLException if a database access error occurs
     * @throws java.sql.SQLTimeoutException when the driver has determined that the timeout value
     *     specified by the {@code setLoginTimeout} method has been exceeded and has at least tried to
     *     cancel the current database connection attempt
     */
    Connection getConn() throws SQLException;

    @Value
    class DsConnGetter implements ConnGetter {
        DataSource dataSource;

        @Override
        public Connection getConn() throws SQLException {
            return dataSource.getConnection();
        }
    }

    @Value
    class JdbcConnGetter implements ConnGetter {
        String url;
        String user;
        String password;

        @Override
        public Connection getConn() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }
    }
}