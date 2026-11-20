package com.tostado.cafeteria.repository;

import com.tostado.cafeteria.config.DatabaseConfig;
import com.tostado.cafeteria.model.Employee;
import java.sql.*;
import java.util.Optional;

/**
 * Acceso a datos de empleados mediante consultas parametrizadas.
 */
public class EmployeeRepository {

    /**
     * Busca un empleado activo por su usuario.
     *
     * @param username nombre de usuario
     * @return el empleado, o vacío si no existe o está inactivo
     * @throws SQLException si falla la consulta
     */
    public Optional<Employee> findByUsername(String username) throws SQLException {
        String sql = "SELECT id,nombre,rol,usuario,activo,contrasena_hash FROM cafeteria_empleado WHERE usuario=? AND activo=1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Employee(rs.getLong("id"), rs.getString("nombre"),
                        rs.getString("rol"), rs.getString("usuario"), rs.getBoolean("activo")));
            }
        }
    }

    /**
     * Obtiene el hash de contraseña de un empleado activo.
     *
     * @param username nombre de usuario
     * @return el hash almacenado, o vacío si no existe
     * @throws SQLException si falla la consulta
     */
    public Optional<String> findPasswordHash(String username) throws SQLException {
        String sql = "SELECT contrasena_hash FROM cafeteria_empleado WHERE usuario=? AND activo=1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty();
            }
        }
    }
}
