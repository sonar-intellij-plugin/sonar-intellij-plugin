package org.intellij.sonar.persistence;

import com.intellij.openapi.components.*;
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
public class SonarResourcesComponent implements PersistentStateComponent<SonarResourcesComponent> {

    public Map<String, List<Resource>> sonarResourcesBySonarServerName = new ConcurrentHashMap<String, List<Resource>>();

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
        XmlSerializerUtil.copyBean(state, this);
    }
}
