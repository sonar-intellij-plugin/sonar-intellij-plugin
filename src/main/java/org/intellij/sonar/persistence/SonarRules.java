package org.intellij.sonar.persistence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.sonarserver.Rule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "rules",
  storages = {
    @Storage(id = "rules", file = StoragePathMacros.PROJECT_FILE),
    @Storage(id = "rulesDir",
             file = StoragePathMacros.PROJECT_CONFIG_DIR+"/sonarRules.xml",
             scheme = StorageScheme.DIRECTORY_BASED)
  }
)
public class SonarRules implements PersistentStateComponent<SonarRules>, ProjectComponent {

  private Map<String,Rule> sonarRulesByRuleKey = new ConcurrentHashMap<String,Rule>();

  public static Optional<SonarRules> getInstance(Project project) {
    return Optional.fromNullable(project.getComponent(SonarRules.class));
  }

  @Nullable
  @Override
  public SonarRules getState() {
    return this;
  }

  @Override
  public void loadState(SonarRules state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  public Map<String,Rule> getSonarRulesByRuleKey() {
    return sonarRulesByRuleKey;
  }

  public void setSonarRulesByRuleKey(Map<String,Rule> sonarRulesByRuleKey) {
    this.sonarRulesByRuleKey = sonarRulesByRuleKey;
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
    return "SonarRules";
  }
}
