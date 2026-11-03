package com.tostado.cafeteria.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {
    @Test void exposesRoleWithoutPassword() {
        Employee employee=new Employee(1,"Admin Demo","administrador","admin",true);
        assertEquals("administrador",employee.rol());
        assertFalse(employee.toString().toLowerCase().contains("password"));
    }
}
