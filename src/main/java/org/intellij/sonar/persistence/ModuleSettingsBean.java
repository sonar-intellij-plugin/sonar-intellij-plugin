package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.module.ModuleSettingsConfigurable;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.sonar.wsclient.services.Resource;

import java.util.ArrayList;
import java.util.Collection;

public class ModuleSettingsBean {
  private String sonarServerName = "";

  private Collection<Resource> resources = new ArrayList<Resource>();

  private Collection<IncrementalScriptBean> scripts = new ArrayList<IncrementalScriptBean>();

  public static ModuleSettingsBean of(String sonarServerName, Collection<Resource> resources, Collection<IncrementalScriptBean> scripts) {
    ModuleSettingsBean bean = new ModuleSettingsBean();
    bean.sonarServerName = sonarServerName;
    bean.resources = resources;
    bean.scripts = scripts;
    return bean;
  }

  public boolean isEmpty() {
    return this.equals(new ModuleSettingsBean());
  }

  public String getSonarServerName() {
    return sonarServerName;
  }

  public Optional<String> getProperServerName(Project project) {
    Optional<String> properServerName = Optional.absent();
    final String name = getSonarServerName();
    if (ModuleSettingsConfigurable.PROJECT_SONAR.equals(name)) {
      final Optional<ProjectSettingsComponent> projectComponent = Optional.fromNullable(project.getComponent(ProjectSettingsComponent.class));
      if (projectComponent.isPresent()) {
        final Optional<ProjectSettingsBean> projectComponentState = Optional.fromNullable(projectComponent.get().getState());
        if (projectComponentState.isPresent()) {
          return projectComponentState.get().getProperSonarServerName();
        }
      }
    } else if (!ProjectSettingsConfigurable.NO_SONAR.equals(name)) {
      return Optional.of(name);
    }
    return properServerName;
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

    ModuleSettingsBean that = (ModuleSettingsBean) o;

    if (resources != null ? !resources.equals(that.resources) : that.resources != null)
      return false;
    if (scripts != null ? !scripts.equals(that.scripts) : that.scripts != null)
      return false;
    if (sonarServerName != null ? !sonarServerName.equals(that.sonarServerName) : that.sonarServerName != null)
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
    return "ModuleSettingsBean{" +
        "sonarServerName='" + sonarServerName + '\'' +
        ", resources=" + resources +
        ", scripts=" + scripts +
        '}';
  }
}
