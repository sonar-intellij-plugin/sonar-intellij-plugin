package org.intellij.sonar.persistence;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "projectSettings",
  storages = {
    @Storage("/sonarSettings.xml")
  }
)
public class ProjectSettings extends AbstractProjectComponent implements PersistentStateComponent<Settings> {

  protected Settings settings = new Settings();

  protected ProjectSettings(Project project) {
    super(project);
  }

  public static ProjectSettings getInstance(Project project) {
    return project.getComponent(ProjectSettings.class);
  }

  @Nullable
  @Override
  public Settings getState() {
    return settings;
  }

  @Override
  public void loadState(@NotNull Settings settings) {
    this.settings = settings;
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "ProjectSettings";
  }
}
