package org.heartattack.heartattacklibs.settings;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerSettingsRepository {
    private final JavaPlugin plugin;
    private final String jdbcUrl;

    public PlayerSettingsRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new IllegalStateException("Failed to create plugin data folder for settings storage.");
        }
        File dbFile = new File(folder, "player-settings.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
    }

    public void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS player_settings (
                  player_uuid TEXT NOT NULL,
                  setting_key TEXT NOT NULL,
                  setting_value INTEGER NOT NULL,
                  updated_at INTEGER NOT NULL,
                  PRIMARY KEY (player_uuid, setting_key)
                )
                """;
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize player settings table.", exception);
        }
    }

    public Map<String, Boolean> load(UUID playerId) {
        Map<String, Boolean> values = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM player_settings WHERE player_uuid = ?";
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    values.put(resultSet.getString("setting_key"), resultSet.getInt("setting_value") == 1);
                }
            }
            return values;
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to load player settings for " + playerId + ".", exception);
        }
    }

    public void upsert(UUID playerId, String key, boolean enabled) {
        String sql = """
                INSERT INTO player_settings (player_uuid, setting_key, setting_value, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(player_uuid, setting_key)
                DO UPDATE SET setting_value = excluded.setting_value, updated_at = excluded.updated_at
                """;
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, key);
            statement.setInt(3, enabled ? 1 : 0);
            statement.setLong(4, System.currentTimeMillis());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to save setting " + key + " for " + playerId + ".", exception);
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}

