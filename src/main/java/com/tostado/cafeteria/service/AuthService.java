package com.tostado.cafeteria.service;

import com.tostado.cafeteria.config.Passwords;
import com.tostado.cafeteria.model.Employee;
import com.tostado.cafeteria.repository.EmployeeRepository;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Lógica de autenticación de empleados.
 */
public class AuthService {

    /** Repositorio de empleados. */
    private final EmployeeRepository repository = new EmployeeRepository();

    /**
     * Verifica usuario y contraseña.
     *
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return el empleado si las credenciales son válidas; vacío en caso contrario
     * @throws SQLException si falla la consulta
     */
    public Optional<Employee> authenticate(String username, String password) throws SQLException {
        Optional<String> hash = repository.findPasswordHash(username);
        if (hash.isEmpty() || !Passwords.verify(password, hash.get())) {
            return Optional.empty();
        }
        return repository.findByUsername(username);
    }
}
