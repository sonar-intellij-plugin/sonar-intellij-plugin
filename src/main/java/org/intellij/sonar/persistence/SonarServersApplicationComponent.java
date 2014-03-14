package org.intellij.sonar.persistence;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@State(
    name = "sonar-servers-application-component",
    storages = {
        @Storage(id = "sonar-servers", file = StoragePathMacros.APP_CONFIG + "/sonar-servers.xml")
    }
)

public class SonarServersApplicationComponent implements PersistentStateComponent<SonarServersApplicationComponent>, ApplicationComponent, Serializable {

  private static final long serialVersionUID = -3112449426227551235L;
  public Set<SonarServerConfigurationBean> beans = new HashSet<SonarServerConfigurationBean>();

  @Transient
  @NotNull
  public static SonarServersApplicationComponent getInstance() {
    return ApplicationManager.getApplication().getComponent(SonarServersApplicationComponent.class);
  }

  @NotNull
  @Override
  public SonarServersApplicationComponent getState() {
    return this;
  }

  @Override
  public void loadState(SonarServersApplicationComponent state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public void initComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void disposeComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "SonarServers";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServersApplicationComponent that = (SonarServersApplicationComponent) o;

    if (beans != null ? !beans.equals(that.beans) : that.beans != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return beans != null ? beans.hashCode() : 0;
  }
}
