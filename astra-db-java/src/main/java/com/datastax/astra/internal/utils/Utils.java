package com.datastax.astra.internal.utils;

/**
 * Utility class for internal usage
 */
public class Utils {

    /**
     * Hide contructors for utilities
     */
    private Utils() {
    }

    public static boolean hasLength(String str) {
        return (null != str && !"".equals(str));
    }
}
