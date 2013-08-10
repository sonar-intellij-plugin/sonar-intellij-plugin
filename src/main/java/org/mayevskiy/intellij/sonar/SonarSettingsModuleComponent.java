package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:37
 */
@State(
    name = "SonarSettingsModuleComponent",
    storages = {
        @Storage(id = "other", file = "$MODULE_FILE$")
    }
)
public class SonarSettingsModuleComponent extends SonarSettingsComponent implements ModuleComponent {
  private Module module;

  public SonarSettingsModuleComponent(Module module) {
    this.module = module;
  }

  @Override
  public void projectOpened() {
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void moduleAdded() {
    if (null == this.getState()) {
      this.loadState(this.module.getProject().getComponent(SonarSettingsProjectComponent.class).getState());
    }
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
