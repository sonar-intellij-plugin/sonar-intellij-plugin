package org.intellij.sonar.configuration;

import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.analysis.SonarQubeInspectionContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;

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

  public static File computeFrom(@NotNull SonarQubeInspectionContext.EnrichedSettings enrichedSettings) {
    File workingDir;
    if (enrichedSettings.settings != null && Boolean.TRUE.equals(enrichedSettings.settings.getUseAlternativeWorkingDir())) {
      workingDir = new File(enrichedSettings.settings.getAlternativeWorkingDirPath());
    } else {
      final String workingDirSelection = withDefaultForModule(enrichedSettings.settings != null ? enrichedSettings.settings.getWorkingDirSelection(): null);
      if (MODULE.equals(workingDirSelection)) {
        if (enrichedSettings.module != null && enrichedSettings.module.getModuleFile() != null) {
          workingDir = new File(enrichedSettings.module.getModuleFile().getParent().getPath());
        } else {
          workingDir = new File(enrichedSettings.project.getBasePath());
        }
      } else if (PROJECT.equals(workingDirSelection)) {
        workingDir = new File(enrichedSettings.project.getBasePath());
      } else {
        workingDir = new File(enrichedSettings.project.getBasePath());
      }
    }
    return workingDir;
  }
}
