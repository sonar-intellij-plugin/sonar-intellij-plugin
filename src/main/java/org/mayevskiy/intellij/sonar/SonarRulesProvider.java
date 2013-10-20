package org.mayevskiy.intellij.sonar;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.settings.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.settings.SonarSettingsComponent;
import org.mayevskiy.intellij.sonar.sonarserver.SonarService;
import org.sonar.wsclient.services.Rule;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Oleg Mayevskiy
 * Date: 01.05.13
 * Time: 12:42
 */
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

  public void syncWithSonar(Project project) {
    final int previousRuleCount = sonarRulesByRuleKey.size();

    clearState();
    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    SonarService sonarService = ServiceManager.getService(SonarService.class);

    for (Rule rule : sonarService.getAllRules(allSonarSettingsBeans)) {
      sonarRulesByRuleKey.put(rule.getKey(), rule);
    }

    int newRuleCount = sonarRulesByRuleKey.size();
    if (previousRuleCount != newRuleCount) {
      // show restart ide dialog
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
  }

  private void clearState() {
    sonarRulesByRuleKey.clear();
  }
}
