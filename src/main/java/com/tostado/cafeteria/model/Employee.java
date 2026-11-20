package com.tostado.cafeteria.model;

import java.io.Serializable;

/**
 * Empleado de la cafetería (sin la contraseña, que nunca se expone).
 *
 * @param id identificador
 * @param nombre nombre completo
 * @param rol rol (por ejemplo "administrador")
 * @param usuario nombre de usuario
 * @param activo si la cuenta está habilitada
 */
public record Employee(long id, String nombre, String rol, String usuario, boolean activo) implements Serializable {}
