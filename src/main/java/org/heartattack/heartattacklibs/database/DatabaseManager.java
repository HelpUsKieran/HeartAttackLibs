package org.heartattack.heartattacklibs.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DatabaseManager {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect(DatabaseConfig config) {
        close();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(plugin.getName() + "-db");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10000L);
        hikariConfig.setLeakDetectionThreshold(15000L);

        if (config.type() == DatabaseType.SQLITE) {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                throw new IllegalStateException("Could not create plugin data folder.");
            }

            String sqliteFile = config.sqliteFile().isBlank() ? "database.db" : config.sqliteFile();
            File databaseFile = new File(dataFolder, sqliteFile);
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        } else {
            hikariConfig.setJdbcUrl(
                    "jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database()
                            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
            );
            hikariConfig.setUsername(config.username());
            hikariConfig.setPassword(config.password());
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection connection() throws SQLException {
        ensureConnected();
        return dataSource.getConnection();
    }

    public int executeUpdate(String sql) {
        return executeUpdate(sql, statement -> {
        });
    }

    public int executeUpdate(String sql, SqlConsumer<PreparedStatement> binder) {
        ensureConnected();
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.accept(statement);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Database update failed for SQL: " + sql, exception);
        }
    }

    public <T> T query(String sql, SqlFunction<ResultSet, T> mapper) {
        return query(sql, statement -> {
        }, mapper);
    }

    public <T> T query(String sql, SqlConsumer<PreparedStatement> binder, SqlFunction<ResultSet, T> mapper) {
        ensureConnected();
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.accept(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapper.apply(resultSet);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Database query failed for SQL: " + sql, exception);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean connected() {
        return dataSource != null && !dataSource.isClosed();
    }

    private void ensureConnected() {
        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException("DatabaseManager is not connected.");
        }
    }
}
