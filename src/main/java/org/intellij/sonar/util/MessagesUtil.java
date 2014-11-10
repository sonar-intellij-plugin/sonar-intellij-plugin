package org.intellij.sonar.util;

public class MessagesUtil {
    public static String warnMessage(String message) {
        return String.format("\nWARNING: %s\n", message);
    }

    public static String errorMessage(String message) {
        return String.format("\nERROR:   %s\n", message);
    }

    public static String okMessage(String message) {
        return String.format("OK:      %s", message);
    }
}
