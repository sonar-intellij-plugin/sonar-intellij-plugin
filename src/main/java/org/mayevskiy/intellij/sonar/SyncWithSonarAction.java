package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.mayevskiy.intellij.sonar.sonarserver.SonarService;
import org.sonar.wsclient.connectors.ConnectionException;

import java.util.Collection;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 14:00
 */
public class SyncWithSonarAction extends DumbAwareAction {

  @Override
  public void actionPerformed(AnActionEvent event) {
    final Project project = event.getProject();
    if (null == project) {
      Messages.showInfoMessage("No project found", "No Project");
      return;
    }

    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    if (allSonarSettingsBeans.isEmpty()) {
      Messages.showMessageDialog("Please configure sonar connection", "No Sonar Configuration", Messages.getWarningIcon());
      return;
    }

    SonarService sonarService = ServiceManager.getService(SonarService.class);
    try {
      sonarService.sync(project);
    } catch (ConnectionException ce) {
      Messages.showMessageDialog("Connection to sonar not successful.\nPlease check if sonar server is running and your project/module connection settings",
          "Sonar Violations", Messages.getErrorIcon());
    }
  }
}
