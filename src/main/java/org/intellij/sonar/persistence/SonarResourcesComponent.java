package org.intellij.sonar.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "sonar-resources-application-component",
  storages = {
    @Storage("/sonar-resources-by-sonar-server-name.xml")
  }
)
public class SonarResourcesComponent implements PersistentStateComponent<SonarResourcesComponent> {

  public Map<String,List<Resource>> sonarResourcesBySonarServerName = new ConcurrentHashMap<>();

  @NotNull
  public static SonarResourcesComponent getInstance() {
    return ServiceManager.getService(SonarResourcesComponent.class);
  }

  @Nullable
  @Override
  public SonarResourcesComponent getState() {
    return this;
  }

  @Override
  public void loadState(SonarResourcesComponent state) {
    XmlSerializerUtil.copyBean(state,this);
  }
}
