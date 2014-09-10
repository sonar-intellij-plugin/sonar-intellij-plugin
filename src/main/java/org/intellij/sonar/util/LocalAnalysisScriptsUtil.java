package org.intellij.sonar.util;

import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.jetbrains.annotations.NotNull;

public final class LocalAnalysisScriptsUtil {
  private LocalAnalysisScriptsUtil() {}

  @NotNull
  public static String withDefaultForProject(String scriptName) {
    if (StringUtil.isEmpty(scriptName)) return LocalAnalysisScripts.NO_LOCAL_ANALYSIS;
    return scriptName;
  }

  @NotNull
  public static String withDefaultForModule(String scriptName) {
    if (StringUtil.isEmpty(scriptName)) return LocalAnalysisScripts.PROJECT;
    return scriptName;
  }
}
