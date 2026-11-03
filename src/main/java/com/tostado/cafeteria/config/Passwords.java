package com.tostado.cafeteria.config;

import org.mindrot.jbcrypt.BCrypt;

public final class Passwords {
    private Passwords() {}
    public static String hash(String password) { return BCrypt.hashpw(password, BCrypt.gensalt(12)); }
    public static boolean verify(String password, String hash) {
        return hash != null && !hash.isBlank() && BCrypt.checkpw(password, hash);
    }
}
