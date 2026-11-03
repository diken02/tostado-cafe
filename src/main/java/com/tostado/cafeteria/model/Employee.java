package com.tostado.cafeteria.model;

import java.io.Serializable;

public record Employee(long id, String nombre, String rol, String usuario, boolean activo) implements Serializable {}
