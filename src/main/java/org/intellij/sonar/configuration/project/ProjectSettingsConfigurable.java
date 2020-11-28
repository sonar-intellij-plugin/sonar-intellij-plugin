package org.intellij.sonar.configuration.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.intellij.sonar.configuration.WorkingDirs;
import org.intellij.sonar.configuration.partials.AlternativeWorkingDirActionListener;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.persistence.SonarConsoleSettings;
import org.intellij.sonar.util.LocalAnalysisScriptsUtil;
import org.intellij.sonar.util.SonarServersUtil;
import org.intellij.sonar.util.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static org.intellij.sonar.util.UIUtil.makeObj;

public class ProjectSettingsConfigurable implements Configurable {

  private final ProjectSettings myProjectSettings;
  private final SonarConsoleSettings mySonarConsoleSettings;
  private final ProjectLocalAnalysisScriptView myLocalAnalysisScriptView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final ProjectSonarServersView mySonarServersView;
  private Project myProject;
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
  private JCheckBox myUseAlternativeWorkingDirCheckBox;
  private JComboBox myWorkingDirComboBox;
  private TextFieldWithBrowseButton myAlternativeWorkingDirTextFieldWithBrowseButton;
  private JCheckBox myShowSonarQubeToolWindowCheckBox;

  public ProjectSettingsConfigurable(Project project) {
    this.myProject = project;
    this.myProjectSettings = ProjectSettings.getInstance(project);
    this.mySonarConsoleSettings = SonarConsoleSettings.getInstance();
    this.mySonarServersView = new ProjectSonarServersView(
      mySonarServersComboBox,
      myAddSonarServerButton,
      myEditSonarServerButton,
      myRemoveSonarServerButton,
      project
    );
    this.myLocalAnalysisScriptView = new ProjectLocalAnalysisScriptView(
      myLocalAnalysisScriptComboBox,
      myAddLocalAnalysisScriptButton,
      myEditLocalAnalysisScriptButton,
      myRemoveLocalAnalysisScriptButton,
      project
    );
    this.mySonarResourcesTableView = new SonarResourcesTableView(project,mySonarServersView);
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
    myPanelForSonarResources.add(mySonarResourcesTableView.getComponent(),BorderLayout.CENTER);
    mySonarServersView.init();
    myLocalAnalysisScriptView.init();
    initWorkingDir();
    initAlternativeWorkingDir();
    return myRootJPanel;
  }

  private void initWorkingDir() {
    myWorkingDirComboBox.removeAllItems();
    myWorkingDirComboBox.addItem(makeObj(WorkingDirs.PROJECT));
    myWorkingDirComboBox.addItem(makeObj(WorkingDirs.MODULE));
  }

  private void initAlternativeWorkingDir() {
    myAlternativeWorkingDirTextFieldWithBrowseButton.addActionListener(
      new AlternativeWorkingDirActionListener(
        myProject,
              myAlternativeWorkingDirTextFieldWithBrowseButton,
              ProjectUtil.guessProjectDir(myProject)
      )
    );
    processAlternativeDirSelections();
    myUseAlternativeWorkingDirCheckBox.addActionListener(
        e -> processAlternativeDirSelections()
    );
  }

  private void processAlternativeDirSelections() {
    myAlternativeWorkingDirTextFieldWithBrowseButton.setEnabled(myUseAlternativeWorkingDirCheckBox.isSelected());
    myWorkingDirComboBox.setEnabled(!myUseAlternativeWorkingDirCheckBox.isSelected());
  }

  @Override
  public boolean isModified() {
    return isProjectSettingsModified() || isSonarConsoleSettings();
  }

  private boolean isProjectSettingsModified() {
    if (null == myProjectSettings) return false;
    Settings state = myProjectSettings.getState();
    return null == state || !state.equals(this.toSettings());
  }

  private boolean isSonarConsoleSettings() {
    if (null == mySonarConsoleSettings) return false;
    SonarConsoleSettings state = mySonarConsoleSettings.getState();
    return null == state || !state.equals(this.toSonarConsoleSettings());
  }

  @Override
  public void apply() {
    myProjectSettings.loadState(this.toSettings());
    mySonarConsoleSettings.loadState(this.toSonarConsoleSettings());
  }

  @Override
  public void reset() {
    if (myProjectSettings != null && myProjectSettings.getState() != null) {
      Settings persistedSettings = myProjectSettings.getState();
      this.setValuesFromSettings(persistedSettings);
    }
    if (mySonarConsoleSettings != null && mySonarConsoleSettings.getState() != null) {
      final SonarConsoleSettings persistedSettings = mySonarConsoleSettings.getState();
      this.setValuesFromSonarConsoleSettings(persistedSettings);
    }
  }

  @Override
  public void disposeUIResources() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  public Settings toSettings() {
    return Settings.of(
      mySonarServersComboBox.getSelectedItem().toString(),
      ImmutableList.copyOf(mySonarResourcesTableView.getTable().getItems()),
      myLocalAnalysisScriptComboBox.getSelectedItem().toString(),
      myWorkingDirComboBox.getSelectedItem().toString(),
      myAlternativeWorkingDirTextFieldWithBrowseButton.getText(),
      myUseAlternativeWorkingDirCheckBox.isSelected()
    );
  }

  public void setValuesFromSettings(Settings settings) {
    if (null == settings) return;
    final String serverName = SonarServersUtil.withDefaultForProject(settings.getServerName());
    UIUtil.selectComboBoxItem(mySonarServersComboBox,serverName);
    final ArrayList<Resource> resources = Lists.newArrayList(settings.getResources());
    mySonarResourcesTableView.setModel(resources);
    final String localAnalysisScripName = LocalAnalysisScriptsUtil.withDefaultForProject(settings
        .getLocalAnalysisScripName());
    UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox,localAnalysisScripName);
    UIUtil.selectComboBoxItem(
      myWorkingDirComboBox,
      WorkingDirs.withDefaultForProject(settings.getWorkingDirSelection())
    );
    myAlternativeWorkingDirTextFieldWithBrowseButton.setText(settings.getAlternativeWorkingDirPath());
    myUseAlternativeWorkingDirCheckBox.setSelected(
        Optional.ofNullable(settings.getUseAlternativeWorkingDir()).orElse(false)
    );
    processAlternativeDirSelections();
  }

  public SonarConsoleSettings toSonarConsoleSettings() {
    return SonarConsoleSettings.of(myShowSonarQubeToolWindowCheckBox.isSelected());
  }

  public void setValuesFromSonarConsoleSettings(SonarConsoleSettings settings) {
    if (null == settings) return;
    myShowSonarQubeToolWindowCheckBox.setSelected(settings.isShowSonarConsoleOnAnalysis());
  }
}
