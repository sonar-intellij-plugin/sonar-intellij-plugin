package org.intellij.sonar.configuration;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

public class WorkingDirs {
  public static final String PROJECT = "<PROJECT>";
  public static final String MODULE = "<MODULE>";

  @NotNull
  public static String withDefaultForProject(String workingDirSelection) {
    if (StringUtil.isEmpty(workingDirSelection)) return PROJECT;
    return workingDirSelection;
  }

  @NotNull
  public static String withDefaultForModule(String workingDirSelection) {
    if (StringUtil.isEmpty(workingDirSelection)) return MODULE;
    return workingDirSelection;
  }
}
