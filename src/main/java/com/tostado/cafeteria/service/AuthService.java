package com.tostado.cafeteria.service;

import com.tostado.cafeteria.config.Passwords;
import com.tostado.cafeteria.model.Employee;
import com.tostado.cafeteria.repository.EmployeeRepository;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final EmployeeRepository repository = new EmployeeRepository();

    public Optional<Employee> authenticate(String username, String password) throws SQLException {
        Optional<String> hash = repository.findPasswordHash(username);
        if (hash.isEmpty() || !Passwords.verify(password, hash.get())) return Optional.empty();
        return repository.findByUsername(username);
    }
}
