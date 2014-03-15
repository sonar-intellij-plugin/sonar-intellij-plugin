package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

@State(
    name = "sonar-servers-application-component",
    storages = {
        @Storage(id = "sonar-servers", file = StoragePathMacros.APP_CONFIG + "/sonar-servers.xml")
    }
)

public class SonarServersService implements PersistentStateComponent<SonarServersService>, Serializable {

  public Collection<SonarServerConfigurationBean> beans = new LinkedList<SonarServerConfigurationBean>();

  @NotNull
  public static SonarServersService getInstance() {
    return ServiceManager.getService(SonarServersService.class);
  }

  @NotNull
  @Override
  public SonarServersService getState() {
    return this;
  }

  @Override
  public void loadState(SonarServersService state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  // generated
  private static final long serialVersionUID = 6992816913889884502L;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServersService that = (SonarServersService) o;

    if (beans != null ? !beans.equals(that.beans) : that.beans != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return beans != null ? beans.hashCode() : 0;
  }
}
