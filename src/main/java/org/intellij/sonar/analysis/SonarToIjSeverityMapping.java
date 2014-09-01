package org.intellij.sonar.analysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.SonarSeverity;

public class SonarToIjSeverityMapping {

  public static HighlightSeverity toHighlightSeverity(String sonarSeverity) {
    if (StringUtils.isBlank(sonarSeverity)) {
      return HighlightSeverity.WARNING;
    } else {
      sonarSeverity = sonarSeverity.toUpperCase();
      if (SonarSeverity.BLOCKER.toString().equals(sonarSeverity) || SonarSeverity.CRITICAL.toString().equals(sonarSeverity)) {
        return HighlightSeverity.ERROR;
      } else if (SonarSeverity.MAJOR.toString().equals(sonarSeverity)) {
        return HighlightSeverity.WARNING;
      } else if (SonarSeverity.INFO.toString().equals(sonarSeverity) || SonarSeverity.MINOR.toString().equals(sonarSeverity)) {
        return HighlightSeverity.WEAK_WARNING;
      } else {
        return HighlightSeverity.WARNING;
      }
    }
  }

  public static ProblemHighlightType toProblemHighlightType(String sonarSeverity) {
    if (StringUtils.isBlank(sonarSeverity)) {
      return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    } else {
      sonarSeverity = sonarSeverity.toUpperCase();
      if (SonarSeverity.BLOCKER.toString().equals(sonarSeverity) || SonarSeverity.CRITICAL.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.ERROR;
      } else if (SonarSeverity.MAJOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      } else if (SonarSeverity.INFO.toString().equals(sonarSeverity) || SonarSeverity.MINOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.WEAK_WARNING;
      } else {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      }
    }
  }
}
