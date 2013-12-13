package org.intellij.sonar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.connectors.ConnectionException;

import java.text.MessageFormat;
import java.util.Collection;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 14:00
 */
public class SyncWithSonarAction extends DumbAwareAction {

  private Project project;

  @Override
  public void actionPerformed(AnActionEvent event) {
    project = event.getProject();
    if (null == project) {
      Messages.showInfoMessage("No project found", "No Project");
      return;
    }

    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    if (allSonarSettingsBeans.isEmpty()) {
      Messages.showMessageDialog("Please configure a sonar connection", "No Sonar Configuration", Messages.getWarningIcon());
      return;
    }

    new SyncWithSonarInBackground(project, "Sync with sonar", true, PerformInBackgroundOption.DEAF).
        setCancelText("Stop sync").
        queue();
  }

  private class SyncWithSonarInBackground extends Task.Backgroundable {

    private SyncWithSonarInBackground(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption) {
      super(project, title, canBeCancelled, backgroundOption);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {

      indicator.setIndeterminate(true);
      indicator.start();

      SonarService sonarService = ServiceManager.getService(SonarService.class);

      try {
        SyncWithSonarResult syncWithSonarResult = sonarService.sync(project, indicator);

        Messages.showMessageDialog(
            MessageFormat.format("Successfully synced with sonar\nDownloaded {0} violations and {1} rules", syncWithSonarResult.violationsCount, syncWithSonarResult.rulesCount),
            "Sonar Sync",
            Messages.getInformationIcon());
      } catch (ConnectionException ce) {
        Messages.showMessageDialog("Connection to sonar not successful.\nPlease check if sonar server is running and your project/module connection settings",
            "Sonar Violations", Messages.getErrorIcon());
      } catch (Exception e) {
        Messages.showMessageDialog("Sync with sonar not successful", "Sonar Sync", Messages.getErrorIcon());
      }
    }
  }
}
