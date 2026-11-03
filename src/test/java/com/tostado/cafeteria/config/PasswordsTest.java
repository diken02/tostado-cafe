package com.tostado.cafeteria.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordsTest {
    @Test void hashesAndVerifiesPassword() {
        String hash=Passwords.hash("admin123");
        assertNotEquals("admin123",hash);
        assertTrue(Passwords.verify("admin123",hash));
        assertFalse(Passwords.verify("wrong",hash));
    }
    @Test void rejectsNullOrBlankHash() {
        assertFalse(Passwords.verify("admin123",null));
        assertFalse(Passwords.verify("admin123",""));
    }
}
