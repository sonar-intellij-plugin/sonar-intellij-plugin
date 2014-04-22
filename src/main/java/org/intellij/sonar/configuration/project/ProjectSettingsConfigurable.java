package org.intellij.sonar.configuration.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.check.ConfigurationCheckActionListener;
import org.intellij.sonar.configuration.partials.IncrementalAnalysisScriptsTableView;
import org.intellij.sonar.configuration.partials.ProjectSonarServersView;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.persistence.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

  public static final String NO_SONAR = "<NO SONAR>";
  private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);


  private final ProjectSettingsComponent myProjectSettingsComponent;
  private final IncrementalAnalysisScriptsTableView myIncrementalAnalysisScriptsTableView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final ProjectSonarServersView mySonarServersView;
  private Project myProject;
  private JButton myCheckConfigurationButton;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JPanel myPanelForIncrementalAnalysisScripts;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;

  public ProjectSettingsConfigurable(Project project) {
    this.myProject = project;
    this.myIncrementalAnalysisScriptsTableView = new IncrementalAnalysisScriptsTableView(myProject);
    this.myProjectSettingsComponent = myProject.getComponent(ProjectSettingsComponent.class);
    this.mySonarServersView = new ProjectSonarServersView(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, myProject);
    this.mySonarResourcesTableView = new SonarResourcesTableView(myProject, mySonarServersView);

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

  @Nullable
  @Override
  public JComponent createComponent() {
    myPanelForSonarResources.setLayout(new BorderLayout());
    myPanelForSonarResources.add(mySonarResourcesTableView.getComponent(), BorderLayout.CENTER);
    myPanelForIncrementalAnalysisScripts.setLayout(new BorderLayout());
    myPanelForIncrementalAnalysisScripts.add(myIncrementalAnalysisScriptsTableView.getComponent(), BorderLayout.CENTER);
    mySonarServersView.init();

    addActionListenerForCheckConfigurationButton();
    return myRootJPanel;
  }

  @Override
  public boolean isModified() {
    if (null == myProjectSettingsComponent) return false;
    ProjectSettingsBean state = myProjectSettingsComponent.getState();
    return null == state || !state.equals(this.toProjectSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    myProjectSettingsComponent.loadState(this.toProjectSettingsBean());
  }

  @Override
  public void reset() {
    if (myProjectSettingsComponent != null && myProjectSettingsComponent.getState() != null) {
      ProjectSettingsBean persistedState = myProjectSettingsComponent.getState();
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
    projectSettingsBean.setSonarServerName(mySonarServersView.getSelectedItemFromComboBox());
    projectSettingsBean.setResources(ImmutableList.copyOf(mySonarResourcesTableView.getTable().getItems()));
    projectSettingsBean.setScripts(ImmutableList.copyOf(myIncrementalAnalysisScriptsTableView.getTable().getItems()));

    return projectSettingsBean;
  }

  public void setValuesFromProjectSettingsBean(ProjectSettingsBean projectSettingsBean) {

    if (null == projectSettingsBean) return;
    mySonarServersView.selectItemForSonarServersComboBoxByName(projectSettingsBean.getSonarServerName());

    final ArrayList<Resource> resources = Lists.newArrayList(projectSettingsBean.getResources());
    mySonarResourcesTableView.setModel(resources);

    final ArrayList<IncrementalScriptBean> scripts = Lists.newArrayList(projectSettingsBean.getScripts());
    myIncrementalAnalysisScriptsTableView.setModel(scripts);
  }

  private void addActionListenerForCheckConfigurationButton() {
    myCheckConfigurationButton.addActionListener(
        new ConfigurationCheckActionListener(
            mySonarServersView,
            myProject,
            mySonarResourcesTableView,
            myIncrementalAnalysisScriptsTableView
        )
    );
  }

}
