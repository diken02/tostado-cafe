package com.tostado.cafeteria.config;

import org.mindrot.jbcrypt.BCrypt;

public final class Passwords {
    private Passwords() {}
    public static String hash(String password) { return BCrypt.hashpw(password, BCrypt.gensalt(12)); }
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
