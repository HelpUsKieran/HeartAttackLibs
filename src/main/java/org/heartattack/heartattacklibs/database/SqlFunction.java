package org.heartattack.heartattacklibs.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T, R> {
    R apply(T value) throws SQLException;
}
