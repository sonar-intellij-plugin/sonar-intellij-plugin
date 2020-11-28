package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.sonarserver.Rule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@State(
  name = "rules",
  storages = {
    @Storage("sonarRules.xml")
  }
)
public class SonarRules implements PersistentStateComponent<SonarRules> {

  private Map<String,Rule> sonarRulesByRuleKey = new ConcurrentHashMap<>();

  public static Optional<SonarRules> getInstance(Project project) {
    return Optional.ofNullable(project.getService(SonarRules.class));
  }

  @Nullable
  @Override
  public SonarRules getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull SonarRules state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  public Map<String,Rule> getSonarRulesByRuleKey() {
    return sonarRulesByRuleKey;
  }

  public void setSonarRulesByRuleKey(Map<String,Rule> sonarRulesByRuleKey) {
    this.sonarRulesByRuleKey = sonarRulesByRuleKey;
  }
}
