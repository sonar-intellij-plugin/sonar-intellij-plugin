package org.intellij.sonar.persistence;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "projectSettings",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/sonarSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
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
