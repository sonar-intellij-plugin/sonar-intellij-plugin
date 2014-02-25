package org.intellij.sonar.util;

public class ThrowableUtils {
  public static String getPrettyStackTraceAsString(Throwable e) {
    return com.google.common.base.Throwables.getStackTraceAsString(e).substring(0, 500) + " ...";
  }
}
