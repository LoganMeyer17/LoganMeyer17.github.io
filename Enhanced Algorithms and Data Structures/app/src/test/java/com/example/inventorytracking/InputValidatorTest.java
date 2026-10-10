package com.example.inventorytracking;

import org.junit.Test;

import static org.junit.Assert.*;

public class InputValidatorTest {
    @Test
    public void usernameAcceptsFourAndTwentyCharacters() {
        assertTrue(InputValidator.isValidUsername("user"));
        assertTrue(InputValidator.isValidUsername("a".repeat(20)));
        assertTrue(InputValidator.isValidUsername("User._-9"));
    }

    @Test
    public void usernameRejectsOutsideLengthLimitsAndUnsupportedCharacters() {
        assertFalse(InputValidator.isValidUsername(null));
        assertFalse(InputValidator.isValidUsername(""));
        assertFalse(InputValidator.isValidUsername("abc"));
        assertFalse(InputValidator.isValidUsername("a".repeat(21)));
        assertFalse(InputValidator.isValidUsername("user!"));
    }

    @Test
    public void usernameDoesNotTrimOrAcceptWhitespace() {
        for (String username : new String[]{" user", "user ", "us er", "us\ter", "us\ner",
                "us\u00A0er", "us\u2003er"}) {
            assertFalse(username, InputValidator.isValidUsername(username));
        }
    }

    @Test
    public void passwordAcceptsEightAndTwentyCharacters() {
        assertTrue(InputValidator.isValidPassword("12345678"));
        assertTrue(InputValidator.isValidPassword("a".repeat(20)));
        assertTrue(InputValidator.isValidPassword("Password!9"));
    }

    @Test
    public void passwordRejectsOutsideLengthLimits() {
        assertFalse(InputValidator.isValidPassword(null));
        assertFalse(InputValidator.isValidPassword(""));
        assertFalse(InputValidator.isValidPassword("1234567"));
        assertFalse(InputValidator.isValidPassword("a".repeat(21)));
    }

    @Test
    public void passwordRejectsWhitespaceWithoutTrimming() {
        for (String password : new String[]{" password1", "password1 ", "pass word1", "pass\tword1",
                "pass\nword1", "pass\rword1", "pass\u00A0word1", "pass\u2003word1", "        "}) {
            assertFalse(password, InputValidator.isValidPassword(password));
        }
    }

    @Test
    public void itemNameRequiresContentAndAllowsOrdinarySpaces() {
        assertTrue(InputValidator.itemNameValid(" Box of bolts "));
        assertTrue(InputValidator.itemNameValid("a".repeat(100)));
        assertFalse(InputValidator.itemNameValid(null));
        assertFalse(InputValidator.itemNameValid(" \t "));
        assertFalse(InputValidator.itemNameValid("\u00A0\u2003"));
        assertFalse(InputValidator.itemNameValid("a".repeat(101)));
    }

    @Test
    public void skuChecksLengthCharactersAndOuterWhitespace() {
        assertTrue(InputValidator.skuValid(" B-1._ "));
        assertTrue(InputValidator.skuValid("b".repeat(40)));
        assertFalse(InputValidator.skuValid(null));
        assertFalse(InputValidator.skuValid(" "));
        assertFalse(InputValidator.skuValid("B 1"));
        assertFalse(InputValidator.skuValid("B'1"));
        assertFalse(InputValidator.skuValid("b".repeat(41)));
    }

    @Test
    public void quantityAcceptsZeroMaximumAndTrimmedDigits() {
        assertEquals(Integer.valueOf(0), InputValidator.parseQuantity("0"));
        assertEquals(Integer.valueOf(Integer.MAX_VALUE), InputValidator.parseQuantity("2147483647"));
        assertEquals(Integer.valueOf(12), InputValidator.parseQuantity(" 0012 "));
    }

    @Test
    public void quantityRejectsMissingNegativeDecimalNonnumericAndOverflow() {
        for (String quantity : new String[]{null, "", " ", "-1", "+1", "1.5", "many", "2147483648"}) {
            assertNull(InputValidator.parseQuantity(quantity));
        }
    }

    @Test
    public void quantityMayReachZeroButCannotGoNegative() {
        assertEquals(Integer.valueOf(0), InputValidator.adjustedQuantity(1, -1));
        assertEquals(Integer.valueOf(1), InputValidator.adjustedQuantity(0, 1));
        assertNull(InputValidator.adjustedQuantity(0, -1));
        assertNull(InputValidator.adjustedQuantity(-1, 1));
    }

    @Test
    public void quantityAdjustmentCannotOverflow() {
        assertEquals(Integer.valueOf(Integer.MAX_VALUE),
                InputValidator.adjustedQuantity(Integer.MAX_VALUE - 1, 1));
        assertNull(InputValidator.adjustedQuantity(Integer.MAX_VALUE, 1));
        assertNull(InputValidator.adjustedQuantity(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }
}
