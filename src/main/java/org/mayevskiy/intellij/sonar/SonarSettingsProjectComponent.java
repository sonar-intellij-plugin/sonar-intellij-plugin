package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;

@State(
        name = "SonarSettingsProjectComponent",
        storages = {
                @Storage(id = "other", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarSettingsProjectComponent extends SonarSettingsComponent implements ProjectComponent {

    public SonarSettingsProjectComponent(Project project) {
    }

}
