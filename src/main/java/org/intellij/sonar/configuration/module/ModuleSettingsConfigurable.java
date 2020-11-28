package org.intellij.sonar.configuration.module;

import static org.intellij.sonar.util.UIUtil.makeObj;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.configuration.WorkingDirs;
import org.intellij.sonar.configuration.partials.AlternativeWorkingDirActionListener;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.persistence.ModuleSettings;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.util.LocalAnalysisScriptsUtil;
import org.intellij.sonar.util.SonarServersUtil;
import org.intellij.sonar.util.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleSettingsConfigurable implements Configurable, ModuleComponent {

  private final ModuleLocalAnalysisScriptView myLocalAnalysisScriptView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final Module myModule;
  private final Project myProject;
  private final ModuleSonarServersView mySonarServersView;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;
  private JComboBox myLocalAnalysisScriptComboBox;
  private JButton myAddLocalAnalysisScriptButton;
  private JButton myEditLocalAnalysisScriptButton;
  private JButton myRemoveLocalAnalysisScriptButton;
  private JComboBox myWorkingDirComboBox;
  private JCheckBox myUseAlternativeWorkingDirCheckBox;
  private TextFieldWithBrowseButton myAlternativeWorkingDirTextFieldWithBrowseButton;

  public ModuleSettingsConfigurable(Module module) {
    this.myModule = module;
    this.myProject = module.getProject();
    this.myLocalAnalysisScriptView = new ModuleLocalAnalysisScriptView(
      myLocalAnalysisScriptComboBox,
      myAddLocalAnalysisScriptButton,
      myEditLocalAnalysisScriptButton,
      myRemoveLocalAnalysisScriptButton,
      myProject
    );
    this.mySonarServersView = new ModuleSonarServersView(
      mySonarServersComboBox,
      myAddSonarServerButton,
      myEditSonarServerButton,
      myRemoveSonarServerButton,
      myProject
    );
    this.mySonarResourcesTableView = new SonarResourcesTableView(myProject,mySonarServersView);
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
    myWorkingDirComboBox.addItem(makeObj(WorkingDirs.MODULE));
    myWorkingDirComboBox.addItem(makeObj(WorkingDirs.PROJECT));
  }

  private void initAlternativeWorkingDir() {
    final VirtualFile moduleFile = myModule.getModuleFile();
    final VirtualFile projectBaseDir = myProject.getBaseDir();
    final VirtualFile dirToSelect = moduleFile != null
      ? moduleFile.getParent()
      : projectBaseDir;
    myAlternativeWorkingDirTextFieldWithBrowseButton.addActionListener(
      new AlternativeWorkingDirActionListener(
        myProject,myAlternativeWorkingDirTextFieldWithBrowseButton,dirToSelect
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
    final ModuleSettings component = ModuleSettings.getInstance(myModule);
    if (null == component) return false;
    Settings state = component.getState();
    return null == state || !state.equals(this.toSettings());
  }

  @Override
  public void apply() {
    Settings settings = this.toSettings();
    ModuleSettings projectSettingsComponent = ModuleSettings.getInstance(myModule);
    projectSettingsComponent.loadState(settings);
  }

  @Override
  public void reset() {
    ModuleSettings moduleSettings = myModule.getService(ModuleSettings.class);
    if (moduleSettings != null && moduleSettings.getState() != null) {
      Settings persistedSettings = moduleSettings.getState();
      this.setValuesFromSettings(persistedSettings);
    }
  }

  @Override
  public void disposeUIResources() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void moduleAdded() {
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
      myLocalAnalysisScriptComboBox.getSelectedItem().toString(),
      myWorkingDirComboBox.getSelectedItem().toString(),
      myAlternativeWorkingDirTextFieldWithBrowseButton.getText(),
      myUseAlternativeWorkingDirCheckBox.isSelected()
    );
  }

  public void setValuesFromSettings(Settings settings) {
    if (null == settings) return;
    final String serverName = SonarServersUtil.withDefaultForModule(settings.getServerName());
    UIUtil.selectComboBoxItem(mySonarServersComboBox,serverName);
    final ArrayList<Resource> resources = Lists.newArrayList(settings.getResources());
    mySonarResourcesTableView.setModel(resources);
    final String localAnalysisScripName = LocalAnalysisScriptsUtil.withDefaultForModule(settings
        .getLocalAnalysisScripName());
    UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox,localAnalysisScripName);
    UIUtil.selectComboBoxItem(myWorkingDirComboBox,WorkingDirs.withDefaultForModule(settings.getWorkingDirSelection()));
    myAlternativeWorkingDirTextFieldWithBrowseButton.setText(settings.getAlternativeWorkingDirPath());
    myUseAlternativeWorkingDirCheckBox.setSelected(
        Optional.ofNullable(settings.getUseAlternativeWorkingDir()).orElse(false)
    );
    processAlternativeDirSelections();
  }
}
