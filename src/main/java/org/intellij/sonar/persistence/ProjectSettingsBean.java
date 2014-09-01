package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import org.sonar.wsclient.services.Resource;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectSettingsBean {
  private String sonarServerName = "";

  private Collection<Resource> resources = new ArrayList<Resource>();

  private Collection<IncrementalScriptBean> scripts = new ArrayList<IncrementalScriptBean>();

  public static ProjectSettingsBean of(String sonarServerName, Collection<Resource> resources, Collection<IncrementalScriptBean> scripts) {
    ProjectSettingsBean bean = new ProjectSettingsBean();
    bean.sonarServerName = sonarServerName;
    bean.resources = resources;
    bean.scripts = scripts;
    return bean;
  }

  public boolean isEmpty() {
    return this.equals(new ProjectSettingsBean());
  }

  public String getSonarServerName() {
    return sonarServerName;
  }

  public Optional<String> getProperSonarServerName() {
    Optional<String> properSonarServerName = Optional.absent();
    final String name = getSonarServerName();
    if (!SonarServers.NO_SONAR.equals(name)) properSonarServerName = Optional.of(name);
    return properSonarServerName;
  }

  public void setSonarServerName(String sonarServerName) {
    this.sonarServerName = sonarServerName;
  }

  public Collection<Resource> getResources() {
    return resources;
  }

  public void setResources(Collection<Resource> resources) {
    this.resources = resources;
  }

  public Collection<IncrementalScriptBean> getScripts() {
    return scripts;
  }

  public void setScripts(Collection<IncrementalScriptBean> scripts) {
    this.scripts = scripts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectSettingsBean bean = (ProjectSettingsBean) o;

    if (resources != null ? !resources.equals(bean.resources) : bean.resources != null)
      return false;
    if (scripts != null ? !scripts.equals(bean.scripts) : bean.scripts != null)
      return false;
    if (sonarServerName != null ? !sonarServerName.equals(bean.sonarServerName) : bean.sonarServerName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sonarServerName != null ? sonarServerName.hashCode() : 0;
    result = 31 * result + (resources != null ? resources.hashCode() : 0);
    result = 31 * result + (scripts != null ? scripts.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ProjectSettingsBean{" +
        "sonarServerName='" + sonarServerName + '\'' +
        ", resources=" + resources +
        ", scripts=" + scripts +
        '}';
  }
}
