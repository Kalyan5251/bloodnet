package com.bloodnet.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password Utility Class for BloodNet Application
 * Provides secure password hashing using SHA-256 with salt
 */
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Generate a random salt
     * @return Base64 encoded salt
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hash a password with salt
     * @param password Plain text password
     * @param salt Base64 encoded salt
     * @return Base64 encoded hashed password
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Decode salt and add to password
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            md.update(saltBytes);
            
            // Hash the password
            byte[] hashedPassword = md.digest(password.getBytes());
            
            return Base64.getEncoder().encodeToString(hashedPassword);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hash algorithm not available: " + HASH_ALGORITHM, e);
        }
    }
    
    /**
     * Hash a password with a new random salt
     * @param password Plain text password
     * @return String array with [0] = hashed password, [1] = salt
     */
    public static String[] hashPasswordWithSalt(String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return new String[]{hashedPassword, salt};
    }
    
    /**
     * Verify a password against a hash
     * @param password Plain text password to verify
     * @param hashedPassword Base64 encoded hashed password
     * @param salt Base64 encoded salt
     * @return true if password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword, String salt) {
        String computedHash = hashPassword(password, salt);
        return computedHash.equals(hashedPassword);
    }
    
    /**
     * Validate password strength
     * @param password Password to validate
     * @return Validation result with message
     */
    public static PasswordValidationResult validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return new PasswordValidationResult(false, "Password must be at least 8 characters long");
        }
        
        if (password.length() > 128) {
            return new PasswordValidationResult(false, "Password must be less than 128 characters");
        }
        
        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        if (!hasUpperCase) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }
        
        if (!hasLowerCase) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }
        
        if (!hasDigit) {
            return new PasswordValidationResult(false, "Password must contain at least one digit");
        }
        
        if (!hasSpecialChar) {
            return new PasswordValidationResult(false, "Password must contain at least one special character");
        }
        
        return new PasswordValidationResult(true, "Password is valid");
    }
    
    /**
     * Inner class for password validation results
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        
        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
