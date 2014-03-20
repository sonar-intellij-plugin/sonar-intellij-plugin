package org.intellij.sonar.persistence;

import org.intellij.sonar.configuration.IncrementalScriptsMapping;
import org.sonar.wsclient.services.Resource;

import java.util.ArrayList;
import java.util.Collection;

public class ProjectSettingsBean {
  public String sonarServerName = "";

  public Collection<Resource> resources = new ArrayList<Resource>();

  public Collection<IncrementalScriptsMapping> scripts = new ArrayList<IncrementalScriptsMapping>();

  public boolean isEmpty() {
    return this.equals(new ProjectSettingsBean());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectSettingsBean that = (ProjectSettingsBean) o;

    if (resources != null ? !resources.equals(that.resources) : that.resources != null) return false;
    if (sonarServerName != null ? !sonarServerName.equals(that.sonarServerName) : that.sonarServerName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sonarServerName != null ? sonarServerName.hashCode() : 0;
    result = 31 * result + (resources != null ? resources.hashCode() : 0);
    return result;
  }
}
