package com.example.loganalyser.util;

public final class RedactionUtil {

    private RedactionUtil() {
    }

    public static String redactSensitiveData(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replaceAll("(?i)password\\s*=\\s*[^\\s]+", "password=***")
                .replaceAll("(?i)token\\s*=\\s*[^\\s]+", "token=***");
    }
}
