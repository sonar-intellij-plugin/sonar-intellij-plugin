package org.intellij.sonar.persistence;

import com.google.common.base.Objects;
import org.sonar.wsclient.services.Resource;

import java.util.ArrayList;
import java.util.Collection;

public class Settings {

  private String serverName;

  private Collection<Resource> resources = new ArrayList<Resource>();

  private String localAnalysisScripName = LocalAnalysisScripts.NO_LOCAL_ANALYSIS;

  public Settings() {}

  public Settings(String serverName, Collection<Resource> resources, String localAnalysisScripName) {
    this.serverName = serverName;
    this.resources = resources;
    this.localAnalysisScripName = localAnalysisScripName;
  }

  public static Settings copyOf(Settings settings) {
    return Settings.of(settings.getServerName(), settings.getResources(), settings.getLocalAnalysisScripName());
  }

  public static Settings of(String serverName, Collection<Resource> resources, String localAnalysisScripName) {
    return new Settings(serverName, resources, localAnalysisScripName);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Settings settings = (Settings) o;

    if (localAnalysisScripName != null ? !localAnalysisScripName.equals(settings.localAnalysisScripName) : settings.localAnalysisScripName != null)
      return false;
    if (resources != null ? !resources.equals(settings.resources) : settings.resources != null)
      return false;
    if (serverName != null ? !serverName.equals(settings.serverName) : settings.serverName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = serverName != null ? serverName.hashCode() : 0;
    result = 31 * result + (resources != null ? resources.hashCode() : 0);
    result = 31 * result + (localAnalysisScripName != null ? localAnalysisScripName.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("serverName", serverName)
        .add("localAnalysisScripName", localAnalysisScripName)
        .add("resources", resources)
        .toString();
  }
}
