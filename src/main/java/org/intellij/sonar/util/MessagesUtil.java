package org.intellij.sonar.util;

public class MessagesUtil {
  public static String warnMessage(String message) {
    return "WARNING: " + message;
  }

  public static String errorMessage(String message) {
    return "ERROR:   " + message;
  }

  public static String okMessage(String message) {
    return "OK:      " + message;
  }
}
