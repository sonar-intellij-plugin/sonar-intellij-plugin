package org.intellij.sonar.configuration.check;

import com.google.common.base.Optional;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;
import org.intellij.sonar.sonarserver.SonarServer;
import org.sonar.wsclient.services.Resource;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static org.intellij.sonar.configuration.check.FileExistenceCheck.checkSourceDirectoryPaths;
import static org.intellij.sonar.configuration.check.IssuesRetrievalCheck.checkIssuesRetrieval;
import static org.intellij.sonar.configuration.check.RulesRetrievalCheck.checkRulesRetrieval;
import static org.intellij.sonar.configuration.check.ScriptExecutionCheck.checkScriptsExecution;
import static org.intellij.sonar.configuration.check.SonarReportContentCheck.checkSonarReportFiles;
import static org.intellij.sonar.util.MessagesUtil.warnMessage;

public class ProjectConfigurationCheckActionListener implements ActionListener {

  private final String mySelectedSonarServerName;
  private final Project myProject;
  private final List<Resource> myResources;
  private final List<IncrementalScriptBean> myIncrementalScriptBeans;

  public ProjectConfigurationCheckActionListener(String mySelectedSonarServerName, Project myProject, List<Resource> myResources, List<IncrementalScriptBean> myIncrementalScriptBeans) {
    this.mySelectedSonarServerName = mySelectedSonarServerName;
    this.myProject = myProject;
    this.myResources = myResources;
    this.myIncrementalScriptBeans = myIncrementalScriptBeans;
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {

    StringBuilder testResultMessageBuilder = new StringBuilder();

    if (ProjectSettingsConfigurable.NO_SONAR.equals(mySelectedSonarServerName)) {
      testResultMessageBuilder.append(warnMessage("No sonar server selected\n"));
    } else {
      final Optional<SonarServerConfigurationBean> sonarServerConfiguration = SonarServersService.get(mySelectedSonarServerName);

      if (!sonarServerConfiguration.isPresent()) {
        testResultMessageBuilder.append(String.format("Cannot find configuration for %s\n", mySelectedSonarServerName));
      } else {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfiguration.get());

        final ConnectionCheck connectionCheck = new ConnectionCheck(sonarServer, myProject).checkSonarServerConnection();
        testResultMessageBuilder.append(connectionCheck.getMessage());
        if (connectionCheck.isOk()) {
          testResultMessageBuilder
              .append(checkRulesRetrieval(sonarServer, myProject, myResources))
              .append(checkIssuesRetrieval(sonarServer, myProject, myResources));
        }
      }
    }

    testResultMessageBuilder.append(checkSourceDirectoryPaths(myProject, myIncrementalScriptBeans))
        .append(checkSonarReportFiles(myProject, myIncrementalScriptBeans))
        .append(checkScriptsExecution(myProject, myIncrementalScriptBeans));

    Messages.showMessageDialog(testResultMessageBuilder.toString(), "Configuration Check Result", AllIcons.Actions.IntentionBulb);

  }
}
