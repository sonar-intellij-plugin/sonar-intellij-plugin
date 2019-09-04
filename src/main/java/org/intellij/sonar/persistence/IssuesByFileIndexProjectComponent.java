package org.intellij.sonar.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.index.SonarIssue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "issues",
  storages = {
    @Storage("sonarIssues.xml")
  }
)
public class IssuesByFileIndexProjectComponent
  implements ProjectComponent, PersistentStateComponent<IssuesByFileIndexProjectComponent> {

  private Map<String,Set<SonarIssue>> index = new HashMap<>();

  public IssuesByFileIndexProjectComponent() {
  }

  public static Optional<IssuesByFileIndexProjectComponent> getInstance(@NotNull Project project) {
    if (project.isDisposed()) return Optional.empty();
    return Optional.ofNullable(project.getComponent(IssuesByFileIndexProjectComponent.class));
  }

  @Nullable
  @Override
  public IssuesByFileIndexProjectComponent getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull IssuesByFileIndexProjectComponent state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  public Map<String,Set<SonarIssue>> getIndex() {
    return index;
  }

  public void setIndex(Map<String,Set<SonarIssue>> index) {
    this.index = index;
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
    return "IssuesByFileIndexProjectComponent";
  }
}
