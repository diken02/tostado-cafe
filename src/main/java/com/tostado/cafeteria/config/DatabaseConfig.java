package com.tostado.cafeteria.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "3306");
        String database = env("DB_NAME", "tostado_cafe");
        String user = env("DB_USER", "root");
        String password = env("DB_PASSWORD", "");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
