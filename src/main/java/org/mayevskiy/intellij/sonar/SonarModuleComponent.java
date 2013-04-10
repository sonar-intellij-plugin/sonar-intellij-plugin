package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:37
 */
@State(
        name = "SonarConfiguration",
        storages = {
                @Storage(id = "other", file = "$MODULE_FILE$")
        }
)
public class SonarModuleComponent extends SonarComponent implements ModuleComponent, PersistentStateComponent<SonarSettingsBean> {
    private SonarSettingsBean sonarSettings;

    private Module module;

    public SonarModuleComponent(Module module) {
        this.module = module;
    }

    @Override
    public void moduleAdded() {
        if (null == this.getState()) {
            this.loadState(this.module.getProject().getComponent(SonarProjectComponent.class).getState());
        }
    }

}
