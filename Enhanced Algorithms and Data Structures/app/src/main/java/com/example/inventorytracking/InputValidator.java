package com.example.inventorytracking;

/** Small, reusable rules that can be tested without running Android. */
public final class InputValidator {
    private InputValidator() { }

    public static boolean isValidUsername(String username) {
        // Check the original input: spaces must be rejected, not silently removed.
        return username != null && username.matches("[A-Za-z0-9._-]{4,20}");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 20) return false;
        for (int index = 0; index < password.length(); index++) {
            char character = password.charAt(index);
            // isSpaceChar also catches nonbreaking spaces that isWhitespace excludes.
            if (Character.isWhitespace(character) || Character.isSpaceChar(character)) return false;
        }
        return true;
    }

    public static boolean itemNameValid(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 100) return false;
        for (int index = 0; index < trimmed.length(); index++) {
            char character = trimmed.charAt(index);
            if (!Character.isWhitespace(character) && !Character.isSpaceChar(character)) return true;
        }
        return false;
    }

    public static boolean skuValid(String sku) {
        return sku != null && sku.trim().matches("[A-Za-z0-9._-]{1,40}");
    }

    /** Returns null for missing, fractional, negative, or out-of-range input. */
    public static Integer parseQuantity(String text) {
        if (text == null || !text.trim().matches("[0-9]+")) return null;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /** Uses long arithmetic so incrementing the maximum int cannot wrap negative. */
    public static Integer adjustedQuantity(int current, int delta) {
        if (current < 0) return null;
        long quantity = (long) current + delta;
        return quantity >= 0 && quantity <= Integer.MAX_VALUE ? (int) quantity : null;
    }
}
