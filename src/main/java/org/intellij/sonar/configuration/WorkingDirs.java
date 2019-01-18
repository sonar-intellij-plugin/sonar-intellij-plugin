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
    return new WorkingDirSelector(enrichedSettings)
            .select()
            .getWorkingDir();
  }

  private static boolean useAlternativeWorkingDir(@NotNull SonarQubeInspectionContext.EnrichedSettings enrichedSettings) {
    return enrichedSettings.settings != null
            && Boolean.TRUE.equals(enrichedSettings.settings
        .getUseAlternativeWorkingDir());
  }

  private static class WorkingDirSelector {
    private final SonarQubeInspectionContext.EnrichedSettings enrichedSettings;

    boolean processing;
    File workingDir;
    private String workingDirSelection;

    WorkingDirSelector(SonarQubeInspectionContext.EnrichedSettings enrichedSettings) {
      this.enrichedSettings = enrichedSettings;
    }

    WorkingDirSelector select() {
      processing = true;
      selectAlternativeWorkingDir();
      if (processing) setWorkingDirSelection();
      if (processing) selectModuleWorkingDir();
      if (processing) selectProjectWorkingDir();
      return this;
    }

    private void selectAlternativeWorkingDir() {
      if (useAlternativeWorkingDir(enrichedSettings)) {
        workingDir = new File(enrichedSettings.settings.getAlternativeWorkingDirPath());
        processing = false;
      }
    }

    private void setWorkingDirSelection() {
      workingDirSelection = withDefaultForModule(
              enrichedSettings.settings != null
                      ? enrichedSettings.settings.getWorkingDirSelection()
                      : null
      );
    }

    private void selectModuleWorkingDir() {
      if (MODULE.equals(workingDirSelection)) {
        if (enrichedSettings.module != null && enrichedSettings.module.getModuleFile() != null) {
          workingDir = new File(enrichedSettings.module.getModuleFile().getParent().getPath());
        } else {
          workingDir = new File(enrichedSettings.project.getBasePath());
        }
        processing = false;
      }
    }

    private void selectProjectWorkingDir() {
        workingDir = new File(enrichedSettings.project.getBasePath());
        processing = false;
    }

    File getWorkingDir() {
      return workingDir;
    }
  }
}
