package org.intellij.sonar;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;

@State(
    name = "SonarSettingsProjectComponent",
    storages = {
        @Storage(id = "other", file = "$PROJECT_FILE$")
    }
)
public class SonarSettingsProjectComponent extends SonarSettingsComponent implements ProjectComponent {

  @SuppressWarnings("UnusedParameters")
  public SonarSettingsProjectComponent(Project project) {
  }

  @Override
  public void projectOpened() {
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return this.getClass().getSimpleName();
  }
}
