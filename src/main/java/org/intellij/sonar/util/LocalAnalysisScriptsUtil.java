package org.intellij.sonar.util;

import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.jetbrains.annotations.NotNull;

public final class LocalAnalysisScriptsUtil {
  private LocalAnalysisScriptsUtil() {}

  @NotNull
  public static String withDefaultForProject(String scriptName) {
    if (null == scriptName) return LocalAnalysisScripts.NO_LOCAL_ANALYSIS;
    return scriptName;
  }

  @NotNull
  public static String withDefaultForModule(String scriptName) {
    if (null == scriptName) return LocalAnalysisScripts.PROJECT;
    return scriptName;
  }
}
