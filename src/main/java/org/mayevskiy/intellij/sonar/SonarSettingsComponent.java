package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:48
 */
public class SonarSettingsComponent implements PersistentStateComponent<SonarSettingsBean> {
    protected SonarSettingsBean sonarSettings;

    public void initComponent() {
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
