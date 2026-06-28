package org.heartattack.heartattacklibs.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseFramework {
    DatabaseManager manager();

    void connect(DatabaseConfig config);

    Connection connection() throws SQLException;

    int update(String sql);

    int update(String sql, SqlConsumer<PreparedStatement> binder);

    <T> T query(String sql, SqlFunction<ResultSet, T> mapper);

    <T> T query(String sql, SqlConsumer<PreparedStatement> binder, SqlFunction<ResultSet, T> mapper);

    boolean connected();

    void close();
}
