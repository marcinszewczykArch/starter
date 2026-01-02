package com.starter.core.util;

/** String utility methods. */
public final class StringUtils {

    private StringUtils() {
        // Utility class
    }

    /**
     * Checks if a string is null, empty, or contains only whitespace.
     *
     * @param str the string to check
     * @return true if null, empty, or blank
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
