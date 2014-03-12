package org.intellij.sonar;

import org.sonar.wsclient.services.Violation;

public class SonarViolationUtils {
  public static boolean isEqual(Violation firstViolation, Violation secondViolation) {
    return null != firstViolation.getRuleKey() && firstViolation.getRuleKey().equals(secondViolation.getRuleKey())
        && null != firstViolation.getLine() && firstViolation.getLine().equals(secondViolation.getLine()) &&
        null != firstViolation.getResourceKey() && firstViolation.getResourceKey().equals(secondViolation.getResourceKey());
  }
}
