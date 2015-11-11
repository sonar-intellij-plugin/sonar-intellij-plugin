package org.intellij.sonar.persistence;

import static com.google.common.base.Optional.fromNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.index.SonarIssue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "issues",
  storages = {
    @Storage(id = "issues", file = StoragePathMacros.PROJECT_FILE),
    @Storage(id = "issuesDir",
             file = StoragePathMacros.PROJECT_CONFIG_DIR+"/sonarIssues.xml",
             scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class IssuesByFileIndexProjectComponent
  implements ProjectComponent, PersistentStateComponent<IssuesByFileIndexProjectComponent> {

  private Map<String,Set<SonarIssue>> index = new HashMap<String,Set<SonarIssue>>();

  public IssuesByFileIndexProjectComponent() {
  }

  public static Optional<IssuesByFileIndexProjectComponent> getInstance(@NotNull Project project) {
    if (project.isDisposed()) return Optional.absent();
    return fromNullable(project.getComponent(IssuesByFileIndexProjectComponent.class));
  }

  @Nullable
  @Override
  public IssuesByFileIndexProjectComponent getState() {
    return this;
  }

  @Override
  public void loadState(IssuesByFileIndexProjectComponent state) {
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
