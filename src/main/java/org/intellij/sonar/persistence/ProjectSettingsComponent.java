package org.intellij.sonar.persistence;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

@State(
    name = "project-settings-component",
    storages = {
        @Storage(id = "project-settings-component" + ProjectSettingsComponent.serialVersionUID, file = StoragePathMacros.PROJECT_FILE)
    }
)
public class ProjectSettingsComponent implements PersistentStateComponent<ProjectSettingsBean>, ProjectComponent, Serializable {

  public static final long serialVersionUID = 6733831949137408777L;

  protected ProjectSettingsBean projectSettingsBean;

  @Nullable
  @Override
  public ProjectSettingsBean getState() {
    return projectSettingsBean;
  }

  @Override
  public void loadState(ProjectSettingsBean projectSettingsBean) {
    this.projectSettingsBean = projectSettingsBean;
  }

  @Override
  public void projectOpened() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void projectClosed() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void disposeComponent() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "ProjectSettings";
  }
}
