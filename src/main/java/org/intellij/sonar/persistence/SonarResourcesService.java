package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = "sonar-resources-application-component",
    storages = {
        @Storage(id = "sonar-resources", file = StoragePathMacros.APP_CONFIG + "/sonar-resources-by-sonar-server-name.xml")
    }
)
public class SonarResourcesService implements PersistentStateComponent<SonarResourcesService> {

  public Map<String, List<Resource>> sonarResourcesBySonarServerName = new ConcurrentHashMap<String, List<Resource>>();

  @NotNull
  public static SonarResourcesService getInstance() {
    return ServiceManager.getService(SonarResourcesService.class);
  }

  @Nullable
  @Override
  public SonarResourcesService getState() {
    return this;
  }

  @Override
  public void loadState(SonarResourcesService state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
