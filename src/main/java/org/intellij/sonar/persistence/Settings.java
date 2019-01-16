package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

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
