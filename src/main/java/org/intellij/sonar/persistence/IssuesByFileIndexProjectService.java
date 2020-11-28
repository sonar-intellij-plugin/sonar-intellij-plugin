package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.index.SonarIssue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@State(
  name = "issues",
  storages = {
    @Storage("sonarIssues.xml")
  }
)
public class IssuesByFileIndexProjectService
  implements PersistentStateComponent<IssuesByFileIndexProjectService> {

  private Map<String,Set<SonarIssue>> index = new HashMap<>();

  public IssuesByFileIndexProjectService() {
  }

  public static Optional<IssuesByFileIndexProjectService> getInstance(@NotNull Project project) {
    if (project.isDisposed()) return Optional.empty();
    return Optional.ofNullable(project.getService(IssuesByFileIndexProjectService.class));
  }

  @Nullable
  @Override
  public IssuesByFileIndexProjectService getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull IssuesByFileIndexProjectService state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  public Map<String,Set<SonarIssue>> getIndex() {
    return index;
  }

  public void setIndex(Map<String,Set<SonarIssue>> index) {
    this.index = index;
  }

}
