package org.intellij.sonar.analysis;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.HighlightSeverity;

import java.util.Map;

class SonarToIjSeverityMapping {

  private static final Map<String, HighlightSeverity> HIGHLIGHT_SEVERITY_BY_SONAR_SEVERITY = ImmutableMap.<String, HighlightSeverity>builder()
          .put("BLOCKER", HighlightSeverity.ERROR)
          .put("CRITICAL", HighlightSeverity.ERROR)
          .put("MAJOR", HighlightSeverity.WARNING)
          .put("INFO", HighlightSeverity.WEAK_WARNING)
          .put("MINOR", HighlightSeverity.WEAK_WARNING)
          .build();

  private static final Map<String, ProblemHighlightType> PROBLEM_HIGHLIGHT_TYPE_BY_SONAR_SEVERITY = ImmutableMap.<String, ProblemHighlightType>builder()
          .put("BLOCKER", ProblemHighlightType.ERROR)
          .put("CRITICAL", ProblemHighlightType.ERROR)
          .put("MAJOR", ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
          .put("INFO", ProblemHighlightType.WEAK_WARNING)
          .put("MINOR", ProblemHighlightType.WEAK_WARNING)
          .build();

  static HighlightSeverity toHighlightSeverity(String sonarSeverity) {
    if (HIGHLIGHT_SEVERITY_BY_SONAR_SEVERITY.containsKey(Strings.nullToEmpty(sonarSeverity))) {
      return HIGHLIGHT_SEVERITY_BY_SONAR_SEVERITY.get(sonarSeverity);
    } else {
      return HighlightSeverity.WARNING;
    }
  }

  static ProblemHighlightType toProblemHighlightType(String sonarSeverity) {
    if (PROBLEM_HIGHLIGHT_TYPE_BY_SONAR_SEVERITY.containsKey(Strings.nullToEmpty(sonarSeverity))) {
      return PROBLEM_HIGHLIGHT_TYPE_BY_SONAR_SEVERITY.get(sonarSeverity);
    } else {
      return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    }
  }
}
