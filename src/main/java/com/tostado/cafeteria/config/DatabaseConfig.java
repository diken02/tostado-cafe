package com.tostado.cafeteria.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Configuración de la conexión JDBC a MySQL, leída de variables de entorno
 * (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD) con valores por defecto.
 */
public final class DatabaseConfig {

    /** Clase de utilidades: no se instancia. */
    private DatabaseConfig() {
    }

    /**
     * Abre una nueva conexión a la base de datos.
     *
     * @return conexión que el llamador debe cerrar
     * @throws SQLException si falta el driver o falla la conexión
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("No se pudo cargar el controlador JDBC de MySQL", exception);
        }
        String host = env("DB_HOST", "localhost");
        String port = env("DB_PORT", "3306");
        String database = env("DB_NAME", "tostado_cafe");
        String user = env("DB_USER", "root");
        String password = env("DB_PASSWORD", "");
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    /** Lee una variable de entorno, usando {@code fallback} si no existe o está vacía. */
    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
