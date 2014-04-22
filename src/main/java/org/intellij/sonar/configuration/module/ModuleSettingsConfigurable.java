package org.intellij.sonar.configuration.module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.check.ConfigurationCheckActionListener;
import org.intellij.sonar.configuration.partials.IncrementalAnalysisScriptsTableView;
import org.intellij.sonar.configuration.partials.ModuleSonarServersView;
import org.intellij.sonar.configuration.partials.SonarResourcesTableView;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.intellij.sonar.persistence.ModuleSettingsBean;
import org.intellij.sonar.persistence.ModuleSettingsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class ModuleSettingsConfigurable implements Configurable, ModuleComponent {

  private static final Logger LOG = Logger.getInstance(ModuleSettingsConfigurable.class);
  public static final String PROJECT_SONAR = "<PROJECT>";
  private final IncrementalAnalysisScriptsTableView myIncrementalAnalysisScriptsTableView;
  private final SonarResourcesTableView mySonarResourcesTableView;
  private final Module myModule;
  private final Project myProject;
  private final ModuleSonarServersView mySonarServersView;
  private JButton myCheckConfigurationButton;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JPanel myPanelForIncrementalAnalysisScripts;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;

  public ModuleSettingsConfigurable(Module module) {
    this.myModule = module;
    this.myProject = module.getProject();
    this.myIncrementalAnalysisScriptsTableView = new IncrementalAnalysisScriptsTableView(myProject);
    this.mySonarServersView = new ModuleSonarServersView(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton,myProject);
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
    final ModuleSettingsComponent component = myModule.getComponent(ModuleSettingsComponent.class);
    if (null == component) return false;
    ModuleSettingsBean state = component.getState();
    return null == state || !state.equals(this.toModuleSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    ModuleSettingsBean moduleSettingsBean = this.toModuleSettingsBean();
    ModuleSettingsComponent projectSettingsComponent = myModule.getComponent(ModuleSettingsComponent.class);
    projectSettingsComponent.loadState(moduleSettingsBean);
  }

  @Override
  public void reset() {
    ModuleSettingsComponent projectSettingsComponent = myModule.getComponent(ModuleSettingsComponent.class);
    if (projectSettingsComponent != null && projectSettingsComponent.getState() != null) {
      ModuleSettingsBean persistedState = projectSettingsComponent.getState();
      this.setValuesFromModuleSettingsBean(persistedState);
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

  public ModuleSettingsBean toModuleSettingsBean() {

    ModuleSettingsBean moduleSettingsBean = new ModuleSettingsBean();
    moduleSettingsBean.setSonarServerName(mySonarServersView.getSelectedItemFromComboBox());
    moduleSettingsBean.setResources(ImmutableList.copyOf(mySonarResourcesTableView.getTable().getItems()));
    moduleSettingsBean.setScripts(ImmutableList.copyOf(myIncrementalAnalysisScriptsTableView.getTable().getItems()));

    return moduleSettingsBean;
  }

  public void setValuesFromModuleSettingsBean(ModuleSettingsBean moduleSettingsBean) {

    if (null == moduleSettingsBean) return;
    mySonarServersView.selectItemForSonarServersComboBoxByName(moduleSettingsBean.getSonarServerName());

    final ArrayList<Resource> resources = Lists.newArrayList(moduleSettingsBean.getResources());
    mySonarResourcesTableView.setModel(resources);

    final ArrayList<IncrementalScriptBean> scripts = Lists.newArrayList(moduleSettingsBean.getScripts());
    myIncrementalAnalysisScriptsTableView.setModel(scripts);
  }

  private void addActionListenerForCheckConfigurationButton() {
    myCheckConfigurationButton.addActionListener(
        new ConfigurationCheckActionListener(
            mySonarServersView,
            myModule.getProject(),
            mySonarResourcesTableView,
            myIncrementalAnalysisScriptsTableView
        )
    );
  }

}
