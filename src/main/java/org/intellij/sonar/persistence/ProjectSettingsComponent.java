package org.intellij.sonar.persistence;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@State(
    name = "project-settings-component",
    storages = {
        @Storage(id = "project-settings-component" + ProjectSettingsComponent.serialVersionUID, file = StoragePathMacros.PROJECT_FILE)
    }
)
public class ProjectSettingsComponent extends AbstractProjectComponent implements PersistentStateComponent<ProjectSettingsBean>, Serializable {

  public static final long serialVersionUID = 6733831949137408777L;

  protected ProjectSettingsBean projectSettingsBean;

  protected ProjectSettingsComponent(Project project) {
    super(project);
  }

  @Nullable
  @Override
  public ProjectSettingsBean getState() {
    return projectSettingsBean;
  }

  @Override
  public void loadState(ProjectSettingsBean projectSettingsBean) {
    this.projectSettingsBean = projectSettingsBean;
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "ProjectSettings";
  }
}
