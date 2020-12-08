package org.intellij.sonar.configuration.module;

import com.intellij.openapi.module.ModuleConfigurationEditor;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationEditorProvider;
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState;

public class SonarQubeModuleConfigurationEditorProvider implements ModuleConfigurationEditorProvider {

    @Override
    public ModuleConfigurationEditor[] createEditors(ModuleConfigurationState state) {
        return new ModuleConfigurationEditor[] {
                new ModuleSettingsConfigurationEditor(state)
        };
    }
}
