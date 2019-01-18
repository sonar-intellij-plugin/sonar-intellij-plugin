package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class Settings {

  private String serverName;
  private Collection<Resource> resources = new ArrayList<>();
  private String localAnalysisScripName;
  private String workingDirSelection;
  private String alternativeWorkingDirPath;
  private Boolean useAlternativeWorkingDir;

  public Settings() {
  }

  public Settings(
    String serverName,
    Collection<Resource> resources,
    String localAnalysisScripName,
    String workingDirSelection,
    String alternativeWorkingDirPath,
    Boolean useAlternativeWorkingDir
  ) {
    this.serverName = serverName;
    this.resources = resources;
    this.localAnalysisScripName = localAnalysisScripName;
    this.workingDirSelection = workingDirSelection;
    this.alternativeWorkingDirPath = alternativeWorkingDirPath;
    this.useAlternativeWorkingDir = useAlternativeWorkingDir;
  }

  public static Settings copyOf(Settings settings) {
    return Settings.of(
      settings.getServerName(),
      settings.getResources(),
      settings.getLocalAnalysisScripName(),
      settings.getWorkingDirSelection(),
      settings.getAlternativeWorkingDirPath(),
      settings.getUseAlternativeWorkingDir()
    );
  }

  public static Settings of(
    String serverName,
    Collection<Resource> resources,
    String localAnalysisScripName,
    String workingDirSelection,
    String alternativeWorkingDirPath,
    Boolean useAlternativeWorkingDir
  ) {
    return new Settings(
      serverName,
      resources,
      localAnalysisScripName,
      workingDirSelection,
      alternativeWorkingDirPath,
      useAlternativeWorkingDir
    );
  }

  public static Settings getSettingsFor(PsiFile psiFile) {
    Settings settings = null;
    Project project = psiFile.getProject();
    VirtualFile virtualFile = psiFile.getVirtualFile();
    if (null != virtualFile) {
      Module module = ModuleUtil.findModuleForFile(virtualFile,project);
      if (null != module) {
        settings = ModuleSettings.getInstance(module).getState();
      }
    } else {
      settings = ProjectSettings.getInstance(project).getState();
    }
    if (null != settings) {
      settings = settings.enrichWithProjectSettings(project);
    }
    return settings;
  }

  public Settings enrichWithProjectSettings(Project project) {
    Settings enrichedSettings = copyOf(this);
    if (SonarServers.PROJECT.equals(this.getServerName())) {
      final Optional<Settings> projectSettings = Optional.ofNullable(ProjectSettings.getInstance(project).getState());
      if (projectSettings.isPresent()) {
        enrichedSettings.setServerName(projectSettings.get().getServerName());
        enrichWithResources(enrichedSettings, projectSettings.get());
        enrichedSettings.setWorkingDirSelection(projectSettings.get().getWorkingDirSelection());
        enrichedSettings.setAlternativeWorkingDirPath(projectSettings.get().getAlternativeWorkingDirPath());
        enrichedSettings.setUseAlternativeWorkingDir(projectSettings.get().getUseAlternativeWorkingDir());
      }
    }
    enrichWithLocalAnalysisScript(project, enrichedSettings);
    return enrichedSettings;
  }

  private void enrichWithResources(Settings processed, Settings projectSettings) {
    if (this.getResources().isEmpty()) {
      processed.setResources(projectSettings.getResources());
    }
  }

  private void enrichWithLocalAnalysisScript(Project project, Settings processed) {
    final String scripName = this.getLocalAnalysisScripName();
    if (LocalAnalysisScripts.PROJECT.equals(scripName)) {
      final Optional<Settings> projectSettings = Optional.ofNullable(ProjectSettings.getInstance(project).getState());
      projectSettings.ifPresent(it -> processed.setLocalAnalysisScripName(it.getLocalAnalysisScripName()));
    }
  }

  public boolean isEmpty() {
    return this.equals(new Settings());
  }

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public Collection<Resource> getResources() {
    return resources;
  }

  public void setResources(Collection<Resource> resources) {
    this.resources = resources;
  }

  public String getLocalAnalysisScripName() {
    return localAnalysisScripName;
  }

  public void setLocalAnalysisScripName(String localAnalysisScripName) {
    this.localAnalysisScripName = localAnalysisScripName;
  }

  public String getWorkingDirSelection() {
    return workingDirSelection;
  }

  public void setWorkingDirSelection(String workingDirSelection) {
    this.workingDirSelection = workingDirSelection;
  }

  public String getAlternativeWorkingDirPath() {
    return alternativeWorkingDirPath;
  }

  public void setAlternativeWorkingDirPath(String alternativeWorkingDirPath) {
    this.alternativeWorkingDirPath = alternativeWorkingDirPath;
  }

  public Boolean getUseAlternativeWorkingDir() {
    return useAlternativeWorkingDir;
  }

  public void setUseAlternativeWorkingDir(Boolean useAlternativeWorkingDir) {
    this.useAlternativeWorkingDir = useAlternativeWorkingDir;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Settings settings = (Settings) o;
    return Objects.equal(serverName, settings.serverName) &&
            Objects.equal(resources, settings.resources) &&
            Objects.equal(localAnalysisScripName, settings.localAnalysisScripName) &&
            Objects.equal(workingDirSelection, settings.workingDirSelection) &&
            Objects.equal(alternativeWorkingDirPath, settings.alternativeWorkingDirPath) &&
            Objects.equal(useAlternativeWorkingDir, settings.useAlternativeWorkingDir);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(serverName, resources, localAnalysisScripName, workingDirSelection, alternativeWorkingDirPath, useAlternativeWorkingDir);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Settings.class.getName())
      .add("serverName",serverName)
      .add("resources",resources)
      .add("localAnalysisScripName",localAnalysisScripName)
      .add("workingDirSelection",workingDirSelection)
      .add("alternativeWorkingDirPath",alternativeWorkingDirPath)
      .add("useAlternativeWorkingDir",useAlternativeWorkingDir)
      .toString();
  }
}
