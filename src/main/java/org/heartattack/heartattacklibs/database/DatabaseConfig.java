package org.heartattack.heartattacklibs.database;

public record DatabaseConfig(
        DatabaseType type,
        String host,
        int port,
        String database,
        String username,
        String password,
        String sqliteFile
) {
    public static DatabaseConfig sqlite(String sqliteFile) {
        return new DatabaseConfig(DatabaseType.SQLITE, "", 0, "", "", "", sqliteFile);
    }

    public static DatabaseConfig mysql(String host, int port, String database, String username, String password) {
        return new DatabaseConfig(DatabaseType.MYSQL, host, port, database, username, password, "");
    }
}
