package org.intellij.sonar.configuration.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.check.ConfigurationCheckActionListener;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.util.LocalAnalysisScriptsUtil;
import org.intellij.sonar.util.SonarServersUtil;
import org.intellij.sonar.util.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

  private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);

  private final ProjectSettings myProjectSettings;
  private final ProjectLocalAnalysisScriptView myLocalAnalysisScriptView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final ProjectSonarServersView mySonarServersView;
  private Project myProject;
  private JButton myCheckConfigurationButton;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;
  private JButton myAddLocalAnalysisScriptButton;
  private JButton myEditLocalAnalysisScriptButton;
  private JButton myRemoveLocalAnalysisScriptButton;
  private JComboBox myLocalAnalysisScriptComboBox;

  public ProjectSettingsConfigurable(Project project) {
    this.myProject = project;
    this.myProjectSettings = ProjectSettings.getInstance(project);
    this.mySonarServersView = new ProjectSonarServersView(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, project);
    this.myLocalAnalysisScriptView = new ProjectLocalAnalysisScriptView(myLocalAnalysisScriptComboBox, myAddLocalAnalysisScriptButton, myEditLocalAnalysisScriptButton, myRemoveLocalAnalysisScriptButton, project);
    this.mySonarResourcesTableView = new SonarResourcesTableView(project, mySonarServersView);
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
    mySonarServersView.init();
    myLocalAnalysisScriptView.init();

    addActionListenerForCheckConfigurationButton();
    return myRootJPanel;
  }

  @Override
  public boolean isModified() {
    if (null == myProjectSettings) return false;
    Settings state = myProjectSettings.getState();
    return null == state || !state.equals(this.toSettings());
  }

  @Override
  public void apply() throws ConfigurationException {
    myProjectSettings.loadState(this.toSettings());
  }

  @Override
  public void reset() {
    if (myProjectSettings != null && myProjectSettings.getState() != null) {
      Settings persistedSettings = myProjectSettings.getState();
      this.setValuesFromSettings(persistedSettings);
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

  public Settings toSettings() {
    return Settings.of(
        mySonarServersComboBox.getSelectedItem().toString(),
        ImmutableList.copyOf(mySonarResourcesTableView.getTable().getItems()),
        myLocalAnalysisScriptComboBox.getSelectedItem().toString()
    );
  }

  public void setValuesFromSettings(Settings settings) {

    if (null == settings) return;
    final String serverName = SonarServersUtil.withDefaultForProject(settings.getServerName());
    UIUtil.selectComboBoxItem(mySonarServersComboBox, serverName);

    final ArrayList<Resource> resources = Lists.newArrayList(settings.getResources());
    mySonarResourcesTableView.setModel(resources);

    final String localAnalysisScripName = LocalAnalysisScriptsUtil.withDefaultForProject(settings.getLocalAnalysisScripName());
    UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox, localAnalysisScripName);
  }

  private void addActionListenerForCheckConfigurationButton() {
    myCheckConfigurationButton.addActionListener(
        new ConfigurationCheckActionListener(
            mySonarServersView,
            myProject,
            mySonarResourcesTableView,
            myLocalAnalysisScriptView
        )
    );
  }


}
