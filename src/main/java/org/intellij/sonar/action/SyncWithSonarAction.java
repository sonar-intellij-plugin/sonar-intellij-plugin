package org.intellij.sonar.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.SyncWithSonarResult;
import org.intellij.sonar.sonarserver.SonarServer;
import org.intellij.sonar.util.ThrowableUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.connectors.ConnectionException;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Collection;

public class SyncWithSonarAction extends DumbAwareAction {

  private Project project;

  @Override
  public void actionPerformed(AnActionEvent event) {
    /*project = event.getProject();
    if (null == project) {
      Messages.showInfoMessage("No project found", "No Project");
      return;
    }

    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    if (allSonarSettingsBeans.isEmpty()) {
      Messages.showMessageDialog("Please configure a sonar connection", "No Sonar Configuration", Messages.getWarningIcon());
      return;
    }

    new SyncWithSonarInBackground(project, "Sync with sonar", true, PerformInBackgroundOption.ALWAYS_BACKGROUND).
        queue();*/
  }

  private class SyncWithSonarInBackground extends Task.Backgroundable {

    public String message;
    public String title;
    public Icon icon;

    private SyncWithSonarInBackground(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption) {
      super(project, title, canBeCancelled, backgroundOption);
    }

    @Override
    public void onSuccess() {
      if (!StringUtil.isEmpty(message)) {
        Messages.showMessageDialog(project, message, title, icon);
      }
    }

    @Override
    public void run(@NotNull final ProgressIndicator indicator) {
      indicator.setIndeterminate(true);
      SonarServer sonarServer = ServiceManager.getService(SonarServer.class);

      title = "Sonar Sync";
      try {
        SyncWithSonarResult syncWithSonarResult = sonarServer.sync(project, indicator);
        message = MessageFormat.format("Successfully synced with sonar\nDownloaded {0} violations and {1} rules", syncWithSonarResult.violationsCount, syncWithSonarResult.rulesCount);
        icon = Messages.getInformationIcon();
      } catch (ConnectionException ce) {
        message = "Connection to sonar not successful.\nPlease check if sonar server is running and your project/module connection settings" +
            "\n\nCause:\n" + ThrowableUtils.getPrettyStackTraceAsString(ce);
        icon = Messages.getErrorIcon();
      } catch (Exception e) {
        message = "Sync with sonar not successful" +
            "\n\nCause:\n" + ThrowableUtils.getPrettyStackTraceAsString(e);
        icon = Messages.getErrorIcon();
      }
    }

  }
}
