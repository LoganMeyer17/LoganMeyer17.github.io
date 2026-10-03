package com.example.inventorytracking;

import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Creates salted password hashes and verifies them without storing plaintext credentials. */
final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 210_000;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() { }

    static PasswordRecord hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        return new PasswordRecord(encode(derive(password, salt)), encode(salt));
    }

    static boolean verify(String password, String encodedHash, String encodedSalt) {
        byte[] expected;
        byte[] salt;
        try {
            expected = Base64.decode(encodedHash, Base64.DEFAULT);
            salt = Base64.decode(encodedSalt, Base64.DEFAULT);
        } catch (IllegalArgumentException exception) {
            return false;
        }
        return MessageDigest.isEqual(expected, derive(password, salt));
    }

    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec specification = new PBEKeySpec(password.toCharArray(), salt,
                ITERATIONS, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Password hashing is unavailable.", exception);
        } finally {
            specification.clearPassword();
        }
    }

    private static String encode(byte[] value) {
        return Base64.encodeToString(value, Base64.NO_WRAP);
    }

    static final class PasswordRecord {
        private final String hash;
        private final String salt;

        PasswordRecord(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }

        String getHash() {
            return hash;
        }

        String getSalt() {
            return salt;
        }
    }
}
