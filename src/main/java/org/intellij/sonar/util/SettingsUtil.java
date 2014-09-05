package org.intellij.sonar.util;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.persistence.SonarServers;

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
  /*public static SonarSettingsBean getSonarSettingsBeanForFile(VirtualFile virtualFile, Project project) {
    SonarSettingsBean sonarSettingsBean = null;
    if (null != project) {
      if (null != virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        if (null != module) {
//          SonarSettingsComponent component = module.getComponent(SonarSettingsModuleComponent.class);
//          sonarSettingsBean = getSonarSettingsBeanFromSonarComponent(component);
        }
      }
      if (null == sonarSettingsBean) {
        sonarSettingsBean = getSonarSettingsBeanFromProject(project);
      }
    }

    return sonarSettingsBean;
  }*/

 /* public static SonarSettingsBean getSonarSettingsBeanFromSonarComponent(SonarSettingsComponent sonarSettingsComponent) {
    return sonarSettingsComponent.getState();
  }*/

  /*public static SonarSettingsBean getSonarSettingsBeanFromProject(Project project) {
//    SonarSettingsProjectComponent sonarProjectComponent = project.getComponent(SonarSettingsProjectComponent.class);
//    return sonarProjectComponent.getState();
    return null;
  }*/
}
