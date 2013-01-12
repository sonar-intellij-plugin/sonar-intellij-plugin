package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "SonarConfiguration",
        storages = {
                @Storage(id = "sonar", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarProjectComponent implements ProjectComponent, PersistentStateComponent<SonarSettingsBean> {
    private SonarSettingsBean sonarSettings;

    public SonarProjectComponent(Project project) {
        this.sonarSettings = SonarSettingsFactory.getInstance();
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "Sonar";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    @Override
    public SonarSettingsBean getState() {
        if (null == sonarSettings) {
            sonarSettings = SonarSettingsFactory.getInstance();
        }
        return sonarSettings;
    }

    @Override
    public void loadState(SonarSettingsBean state) {
        this.sonarSettings = state;
    }
}
