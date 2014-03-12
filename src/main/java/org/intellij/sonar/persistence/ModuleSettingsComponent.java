package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "module-settings-component",
    storages = {
        @Storage(id = "module-settings-component", file = BaseDir.PATH + "module-settings.xml")
    }
)
public class ModuleSettingsComponent implements PersistentStateComponent<ModuleSettingsBean>, ModuleComponent {
  protected ModuleSettingsBean projectSettingsBean;

  @Nullable
  @Override
  public ModuleSettingsBean getState() {
    return projectSettingsBean;
  }

  @Override
  public void loadState(ModuleSettingsBean projectSettingsBean) {
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
  public void moduleAdded() {

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
