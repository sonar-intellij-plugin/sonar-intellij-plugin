package org.intellij.sonar;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Rule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = "SonarRulesProvider",
    storages = {
        @Storage(id = "other", file = "$PROJECT_FILE$")
    }
)
public class SonarRulesProvider implements PersistentStateComponent<SonarRulesProvider> {

  public Map<String, Rule> sonarRulesByRuleKey;

  public SonarRulesProvider() {
    this.sonarRulesByRuleKey = new ConcurrentHashMap<String, Rule>();
  }

  @SuppressWarnings("UnusedDeclaration")
  public SonarRulesProvider(Project project) {
    this();
  }

  @Nullable
  @Override
  public SonarRulesProvider getState() {
    // workaround NullPointerException of XmlSerializerUtil during serialisation
    // can be set to null because we don't need this info
    for (Rule sonarRule : sonarRulesByRuleKey.values()) {
      sonarRule.setParams(null);
    }
    return this;
  }

  @Override
  public void loadState(SonarRulesProvider state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public int syncWithSonar(Project project, @NotNull ProgressIndicator indicator) {
    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    SonarService sonarService = ServiceManager.getService(SonarService.class);

    Map<String, Rule> sonarRulesByRuleKeyFromServer = new HashMap<String, Rule>();
    for (Rule rule : sonarService.getAllRules(allSonarSettingsBeans, indicator)) {
      sonarRulesByRuleKeyFromServer.put(rule.getKey(), rule);
    }

    if (!equalMaps(sonarRulesByRuleKey, sonarRulesByRuleKeyFromServer)) {
      sonarRulesByRuleKey.clear();
      sonarRulesByRuleKey.putAll(sonarRulesByRuleKeyFromServer);
      showRestartIdeDialog();
    }

    return sonarRulesByRuleKey.size();
  }

  private void showRestartIdeDialog() {
    final int ret = Messages.showOkCancelDialog("Detected new sonar rules. You have to restart IDE to reload the settings. Restart?",
        IdeBundle.message("title.restart.needed"), Messages.getQuestionIcon());
    if (ret == 0) {
      if (ApplicationManager.getApplication().isRestartCapable()) {
        ApplicationManager.getApplication().restart();
      } else {
        ApplicationManager.getApplication().exit();
      }
    }
  }

  private boolean equalMaps(Map<String, Rule> m1, Map<String, Rule> m2) {
    if (m1.size() != m2.size())
      return false;
    for (String key1 : m1.keySet()) {
      if (!m2.containsKey(key1)) {
        return false;
      }
    }
    return true;
  }
}
