package org.intellij.sonar.configuration.project;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import org.intellij.sonar.configuration.IncrementalScriptsMapping;
import org.intellij.sonar.configuration.SonarResourceMapping;
import org.intellij.sonar.configuration.SonarServerConfigurable;
import org.intellij.sonar.persistence.ProjectSettingsBean;
import org.intellij.sonar.persistence.ProjectSettingsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

  private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);
  private final TableView<SonarResourceMapping> sonarResourcesTable;
  private final TableView<IncrementalScriptsMapping> incrementalAnalysisScriptsTable;
  private Project myProject;
  private JButton testConfigurationButton;
  private JPanel rootJPanel;
  private JPanel panelForSonarResources;
  private JPanel panelForIncrementalAnalysisScripts;
  private JComboBox sonarServersComboBox;
  private JButton addSonarServerButton;
  private JButton editSonarServerButton;
  private JButton removeSonarServerButton;

  public ProjectSettingsConfigurable(Project project) {
    this.myProject = project;
    this.sonarResourcesTable = new TableView<SonarResourceMapping>();
    this.incrementalAnalysisScriptsTable = new TableView<IncrementalScriptsMapping>();
  }

  public Project getMyProject() {
    return myProject;
  }

  public JPanel getRootJPanel() {
    return rootJPanel;
  }

  private JComponent createSonarResourcesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(sonarResourcesTable, null).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  private JComponent createIncrementalAnalysisScriptsTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(incrementalAnalysisScriptsTable, null).
        setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {

          }
        }).
        setEditActionName("Edit").
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "SonarQube";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  public JButton getTestConfigurationButton() {
    return testConfigurationButton;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    addActionListenersForSonarServerButtons();

    panelForSonarResources.setLayout(new BorderLayout());
    panelForSonarResources.add(createSonarResourcesTable(), BorderLayout.CENTER);
    panelForIncrementalAnalysisScripts.setLayout(new BorderLayout());
    panelForIncrementalAnalysisScripts.add(createIncrementalAnalysisScriptsTable(), BorderLayout.CENTER);

    return getRootJPanel();
  }

  private void addActionListenersForSonarServerButtons() {
    addSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final SonarServerConfigurable dlg = new SonarServerConfigurable(myProject);
        dlg.show();
        if (dlg.isOK()) {
          LOG.info("OK");
        }
      }
    });
    editSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        LOG.info("edit sonar server button clicked");
      }
    });
    removeSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        LOG.info("remove sonar server button clicked");
      }
    });
  }

//  private void makePasswordInvisible() {
//    getPasswordField().setEchoChar('•');
//  }
//
//  private void makePasswordVisible() {
//    getPasswordField().setEchoChar('\u0000');
//  }

//  private void setCredentialsEnabled() {
//    getUserTextField().setEnabled(!getUseAnonymousCheckBox().isSelected());
//    getPasswordField().setEnabled(!getUseAnonymousCheckBox().isSelected());
//  }

  @Override
  public boolean isModified() {
    ProjectSettingsBean state = myProject.getComponent(ProjectSettingsComponent.class).getState();
    return null == state || !state.equals(this.toProjectSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    ProjectSettingsBean projectSettingsBean = this.toProjectSettingsBean();
    ProjectSettingsComponent projectSettingsComponent = myProject.getComponent(ProjectSettingsComponent.class);
    projectSettingsComponent.loadState(projectSettingsBean);
  }

  @Override
  public void reset() {
    ProjectSettingsComponent projectSettingsComponent = myProject.getComponent(ProjectSettingsComponent.class);
    if (projectSettingsComponent != null && projectSettingsComponent.getState() != null) {
      ProjectSettingsBean persistedState = projectSettingsComponent.getState();
      this.setValuesFromProjectSettingsBean(persistedState);
    }
  }

  @Override
  public void disposeUIResources() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void projectOpened() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void projectClosed() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initComponent() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void disposeComponent() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "SonarQube";
  }

  public ProjectSettingsBean toProjectSettingsBean() {

    ProjectSettingsBean projectSettingsBean = new ProjectSettingsBean();

    ProjectSettingsBean persistedProjectSettingsBean = myProject.getComponent(ProjectSettingsComponent.class).getState();
    if (persistedProjectSettingsBean != null) {
//      projectSettingsBean.downloadedResources = persistedProjectSettingsBean.downloadedResources;
    }
    convertResourcesListToBean(projectSettingsBean);

    return projectSettingsBean;
  }

  private void convertResourcesListToBean(ProjectSettingsBean projectSettingsBean) {
//    ListModel model = this.getResourcesList().getModel();
//    projectSettingsBean.resources = new ArrayList<String>(model.getSize());
//    for (int i = 0; i < model.getSize(); i++) {
//      projectSettingsBean.resources.add((String) model.getElementAt(i));
//    }
  }

  public void setValuesFromProjectSettingsBean(ProjectSettingsBean projectSettingsBean) {
    if (null == projectSettingsBean) return;
  }

}
