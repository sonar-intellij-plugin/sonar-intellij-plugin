package org.intellij.sonar.configuration.check;

import com.google.common.base.Optional;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.partials.IncrementalAnalysisScriptsTableView;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.configuration.partials.SonarServersView;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersComponent;
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

public class ConfigurationCheckActionListener implements ActionListener {

  private final Project myProject;
  private final SonarServersView mySonarServersView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final IncrementalAnalysisScriptsTableView myIncrementalAnalysisScriptsTableView;

  public ConfigurationCheckActionListener(SonarServersView sonarServersView, Project myProject, SonarResourcesTableView sonarResourcesTableView, IncrementalAnalysisScriptsTableView incrementalAnalysisScriptsTableView) {
    this.mySonarServersView = sonarServersView;
    this.myProject = myProject;
    this.mySonarResourcesTableView = sonarResourcesTableView;
    this.myIncrementalAnalysisScriptsTableView = incrementalAnalysisScriptsTableView;
  }

  protected String getSelectedSonarServerName() {
    return mySonarServersView.getSelectedItem();
  }

  private List<IncrementalScriptBean> getIncrementalScriptBeans() {
    return myIncrementalAnalysisScriptsTableView.getTable().getItems();
  }

  private List<Resource> getResources() {
    return mySonarResourcesTableView.getTable().getItems();
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {

    StringBuilder testResultMessageBuilder = new StringBuilder();

    if (ProjectSettingsConfigurable.NO_SONAR.equals(getSelectedSonarServerName())) {
      testResultMessageBuilder.append(warnMessage("No sonar server selected\n"));
    } else {
      final Optional<SonarServerConfigurationBean> sonarServerConfiguration = SonarServersComponent.get(getSelectedSonarServerName());

      if (!sonarServerConfiguration.isPresent()) {
        testResultMessageBuilder.append(String.format("Cannot find configuration for %s\n", getSelectedSonarServerName()));
      } else {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfiguration.get());

        final ConnectionCheck connectionCheck = new ConnectionCheck(sonarServer, myProject).checkSonarServerConnection();
        testResultMessageBuilder.append(connectionCheck.getMessage());
        if (connectionCheck.isOk()) {
          testResultMessageBuilder
              .append(checkRulesRetrieval(sonarServer, myProject, getResources()))
              .append(checkIssuesRetrieval(sonarServer, myProject, getResources()));
        }
      }
    }

    testResultMessageBuilder.append(checkSourceDirectoryPaths(myProject, getIncrementalScriptBeans()))
        .append(checkSonarReportFiles(myProject, getIncrementalScriptBeans()))
        .append(checkScriptsExecution(myProject, getIncrementalScriptBeans()));

    Messages.showMessageDialog(testResultMessageBuilder.toString(), "Configuration Check Result", AllIcons.Actions.IntentionBulb);

  }
}
