package org.mayevskiy.intellij.sonar.component;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.bean.SonarSettingsFactory;

@State(
        name = "SonarConfiguration",
        storages = {
                @Storage(id = "sonar", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarProjectComponent implements ProjectComponent, PersistentStateComponent<SonarSettingsBean> {
    private SonarSettingsBean sonarSettings;

    public SonarProjectComponent(Project project) {
    }

    public void initComponent() {
        this.sonarSettings = SonarSettingsFactory.getInstance();
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
        return sonarSettings;
    }

    @Override
    public void loadState(SonarSettingsBean state) {
        this.sonarSettings = state;
    }
}
