package org.mayevskiy.intellij.sonar.component;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;

@State(
        name = "SonarConfiguration",
        storages = {
                @Storage(id = "other", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarProjectComponent extends SonarComponent implements ProjectComponent {

    public SonarProjectComponent(Project project) {
    }

}
