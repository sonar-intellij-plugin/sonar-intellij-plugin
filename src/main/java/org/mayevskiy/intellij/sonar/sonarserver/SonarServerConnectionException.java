package org.mayevskiy.intellij.sonar.sonarserver;

/**
 * @author Michail Plushnikov
 */
public class SonarServerConnectionException extends Exception {
  public SonarServerConnectionException(String message, Object... params) {
    super(String.format(message, params));
  }

  public SonarServerConnectionException(String message, Throwable cause, Object... params) {
    super(String.format(message, params), cause);
  }
}
