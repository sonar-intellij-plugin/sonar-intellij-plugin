package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.index2.SonarIssue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;

@State(
    name = "issuesByFileIndex",
    storages = {
        @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
        @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/sonarSettings.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class IssuesByFileIndexProjectComponent extends AbstractProjectComponent implements PersistentStateComponent<Map<String, Set<SonarIssue>>> {

  private Map<String, Set<SonarIssue>> index = Maps.newConcurrentMap();

  protected IssuesByFileIndexProjectComponent(Project project) {
    super(project);
  }

  public static Optional<IssuesByFileIndexProjectComponent> getInstance(@NotNull Project project) {
    if (project.isDisposed()) return Optional.absent();
    return fromNullable(project.getComponent(IssuesByFileIndexProjectComponent.class));
  }


  @Nullable
  @Override
  public Map<String, Set<SonarIssue>> getState() {
    return index;
  }

  @Override
  public void loadState(Map<String, Set<SonarIssue>> index) {
    this.index = index;
  }
}
