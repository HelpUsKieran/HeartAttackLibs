package org.heartattack.heartattacklibs.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseFrameworkImpl implements DatabaseFramework {
    private final DatabaseManager manager;

    public DatabaseFrameworkImpl(DatabaseManager manager) {
        this.manager = manager;
    }

    @Override
    public DatabaseManager manager() {
        return manager;
    }

    @Override
    public void connect(DatabaseConfig config) {
        manager.connect(config);
    }

    @Override
    public Connection connection() throws SQLException {
        return manager.connection();
    }

    @Override
    public int update(String sql) {
        return manager.executeUpdate(sql);
    }

    @Override
    public int update(String sql, SqlConsumer<PreparedStatement> binder) {
        return manager.executeUpdate(sql, binder);
    }

    @Override
    public <T> T query(String sql, SqlFunction<ResultSet, T> mapper) {
        return manager.query(sql, mapper);
    }

    @Override
    public <T> T query(String sql, SqlConsumer<PreparedStatement> binder, SqlFunction<ResultSet, T> mapper) {
        return manager.query(sql, binder, mapper);
    }

    @Override
    public boolean connected() {
        return manager.connected();
    }

    @Override
    public void close() {
        manager.close();
    }
}
