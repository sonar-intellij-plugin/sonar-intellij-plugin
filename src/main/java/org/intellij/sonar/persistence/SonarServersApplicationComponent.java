package org.intellij.sonar.persistence;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@State(
    name = "sonar-servers-application-component",
    storages = {
        @Storage(id = "sonar-servers-application-component", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/sonar-servers.xml")
    }
)

public class SonarServersApplicationComponent implements PersistentStateComponent<List<SonarServerConfigurationBean>>, ApplicationComponent {
  List<SonarServerConfigurationBean> listOfSonarServerConfigurationBeans;

  @Nullable
  @Override
  public List<SonarServerConfigurationBean> getState() {
    return listOfSonarServerConfigurationBeans;
  }

  @Override
  public void loadState(List<SonarServerConfigurationBean> listOfSonarServerConfigurationBeans) {
    this.listOfSonarServerConfigurationBeans = listOfSonarServerConfigurationBeans;
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
}
