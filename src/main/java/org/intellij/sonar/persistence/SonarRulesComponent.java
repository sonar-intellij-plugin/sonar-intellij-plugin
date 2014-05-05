package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = "sonar-rules-component",
    storages = {
        @Storage(id = "sonar-rules-component" + SonarRulesComponent.serialVersionUID, file = StoragePathMacros.PROJECT_FILE)
    }
)
public class SonarRulesComponent implements PersistentStateComponent<SonarRulesComponent>, Serializable {

  public static final long serialVersionUID = 5793406347928764164L;
  private Map<String, SonarRuleBean> sonarRulesByRuleKey;

  public SonarRulesComponent() {
    this.sonarRulesByRuleKey = new ConcurrentHashMap<String, SonarRuleBean>();
  }

  @Nullable
  @Override
  public SonarRulesComponent getState() {
    return this;
  }

  @Override
  public void loadState(SonarRulesComponent state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public Map<String, SonarRuleBean> getSonarRulesByRuleKey() {
    return sonarRulesByRuleKey;
  }

  public void setSonarRulesByRuleKey(Map<String, SonarRuleBean> sonarRulesByRuleKey) {
    this.sonarRulesByRuleKey = sonarRulesByRuleKey;
  }
}
