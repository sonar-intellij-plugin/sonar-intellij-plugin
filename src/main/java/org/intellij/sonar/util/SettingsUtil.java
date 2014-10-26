package org.intellij.sonar.util;

import com.google.common.base.Optional;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.persistence.*;

public class SettingsUtil {

  public static Settings process(Project project, Settings settings) {
    Settings processed = Settings.copyOf(settings);
    final String serverName = settings.getServerName();
    if (SonarServers.PROJECT.equals(serverName)) {
      final Optional<Settings> projectSettings = Optional.fromNullable(ProjectSettings.getInstance(project).getState());
      if (projectSettings.isPresent()) {
        processed.setServerName(projectSettings.get().getServerName());
        if (settings.getResources().isEmpty()) {
          processed.setResources(projectSettings.get().getResources());
        }
        processed.setWorkingDirSelection(projectSettings.get().getWorkingDirSelection());
        processed.setAlternativeWorkingDirPath(projectSettings.get().getAlternativeWorkingDirPath());
        processed.setUseAlternativeWorkingDir(projectSettings.get().getUseAlternativeWorkingDir());
      }
    }
    final String scripName = settings.getLocalAnalysisScripName();
    if (LocalAnalysisScripts.PROJECT.equals(scripName)) {
      final Optional<Settings> projectSettings = Optional.fromNullable(ProjectSettings.getInstance(project).getState());
      if (projectSettings.isPresent()) {
        processed.setLocalAnalysisScripName(projectSettings.get().getLocalAnalysisScripName());
      }
    }

    return processed;
  }
  public static Settings getSettingsFor(PsiFile psiFile) {
    Settings settings = null;
    Project project = psiFile.getProject();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (null != virtualFile) {
      Module module = ModuleUtil.findModuleForFile(virtualFile, project);
      if (null != module) {
        settings = ModuleSettings.getInstance(module).getState();
      }
    } else {
      settings = ProjectSettings.getInstance(project).getState();
    }
    settings = process(project, settings);

    return settings;
  }

 /* public static SonarSettingsBean getSonarSettingsBeanFromSonarComponent(SonarSettingsComponent sonarSettingsComponent) {
    return sonarSettingsComponent.getState();
  }*/

  /*public static SonarSettingsBean getSonarSettingsBeanFromProject(Project project) {
//    SonarSettingsProjectComponent sonarProjectComponent = project.getComponent(SonarSettingsProjectComponent.class);
//    return sonarProjectComponent.getState();
    return null;
  }*/
}
