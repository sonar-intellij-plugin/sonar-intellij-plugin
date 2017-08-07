package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Objects;
import org.sonar.wsclient.services.Resource;

public class Settings {

  private String serverName;
  private Collection<Resource> resources = new ArrayList<Resource>();
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

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Settings settings = (Settings) o;
    if (alternativeWorkingDirPath != null
      ? !alternativeWorkingDirPath.equals(settings.alternativeWorkingDirPath)
      : settings.alternativeWorkingDirPath != null)
      return false;
    if (localAnalysisScripName != null
      ? !localAnalysisScripName.equals(settings.localAnalysisScripName)
      : settings.localAnalysisScripName != null)
      return false;
    if (resources != null
      ? !resources.equals(settings.resources)
      : settings.resources != null)
      return false;
    if (serverName != null
      ? !serverName.equals(settings.serverName)
      : settings.serverName != null)
      return false;
    if (useAlternativeWorkingDir != null
      ? !useAlternativeWorkingDir.equals(settings.useAlternativeWorkingDir)
      : settings.useAlternativeWorkingDir != null)
      return false;
    if (workingDirSelection != null
      ? !workingDirSelection.equals(settings.workingDirSelection)
      : settings.workingDirSelection != null)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = serverName != null
      ? serverName.hashCode()
      : 0;
    result = 31 * result+(resources != null
      ? resources.hashCode()
      : 0);
    result = 31 * result+(localAnalysisScripName != null
      ? localAnalysisScripName.hashCode()
      : 0);
    result = 31 * result+(workingDirSelection != null
      ? workingDirSelection.hashCode()
      : 0);
    result = 31 * result+(alternativeWorkingDirPath != null
      ? alternativeWorkingDirPath.hashCode()
      : 0);
    result = 31 * result+(useAlternativeWorkingDir != null
      ? useAlternativeWorkingDir.hashCode()
      : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this.getClass().getName())
      .add("serverName",serverName)
      .add("resources",resources)
      .add("localAnalysisScripName",localAnalysisScripName)
      .add("workingDirSelection",workingDirSelection)
      .add("alternativeWorkingDirPath",alternativeWorkingDirPath)
      .add("useAlternativeWorkingDir",useAlternativeWorkingDir)
      .toString();
  }
}
