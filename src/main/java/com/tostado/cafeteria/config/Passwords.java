package com.tostado.cafeteria.config;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidades para cifrar y verificar contraseñas con BCrypt.
 */
public final class Passwords {

    /** Clase de utilidades: no se instancia. */
    private Passwords() {
    }

    /**
     * Genera el hash BCrypt de una contraseña.
     *
     * @param password contraseña en texto plano
     * @return hash con sal incluida
     */
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Comprueba una contraseña contra su hash.
     *
     * @param password contraseña en texto plano
     * @param hash hash BCrypt almacenado
     * @return {@code true} solo si el hash es válido y coincide
     */
    public static boolean verify(String password, String hash) {
        if (password == null || hash == null || hash.isBlank()) {
            return false;
        }
        if (!hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
