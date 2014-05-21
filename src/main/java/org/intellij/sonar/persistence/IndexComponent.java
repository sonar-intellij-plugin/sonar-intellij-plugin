package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Optional.fromNullable;

@State(
    name = "index-component",
    storages = {
        @Storage(id = "index-component" + IndexComponent.serialVersionUID , file = StoragePathMacros.PROJECT_FILE)
    }
)
public class IndexComponent extends AbstractProjectComponent implements PersistentStateComponent<Map<IssuesIndexKey, Set<IssuesIndexEntry>>>, Serializable {

  public static final long serialVersionUID = -2073972896207461743L;

  public static Optional<IndexComponent> getInstance(@NotNull Project project) {
    if (project.isDisposed()) return Optional.absent();
    return fromNullable(project.getComponent(IndexComponent.class));
  }

  private Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex = new ConcurrentHashMap<IssuesIndexKey, Set<IssuesIndexEntry>>();

  public IndexComponent(Project project) {
    super(project);
  }

  @Nullable
  @Override
  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> getState() {
    return issuesIndex;
  }

  @Override
  public void loadState(Map<IssuesIndexKey, Set<IssuesIndexEntry>> state) {
    this.issuesIndex = state;
  }

}
