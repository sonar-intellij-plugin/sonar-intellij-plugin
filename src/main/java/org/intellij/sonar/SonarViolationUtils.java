package org.intellij.sonar;

import org.sonar.wsclient.services.Violation;

/**
 * Author: Oleg Mayevskiy
 * Date: 11.04.13
 * Time: 14:27
 */
public class SonarViolationUtils {
  public static boolean isEqual(Violation firstViolation, Violation secondViolation) {
    return null != firstViolation.getRuleKey() && firstViolation.getRuleKey().equals(secondViolation.getRuleKey())
        && null != firstViolation.getLine() && firstViolation.getLine().equals(secondViolation.getLine()) &&
        null != firstViolation.getResourceKey() && firstViolation.getResourceKey().equals(secondViolation.getResourceKey());
  }
}
