package org.intellij.sonar.configuration.module;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.partials.SonarServersView;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.ProjectSettingsBean;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;

import javax.swing.*;
import java.util.Collection;

import static org.intellij.sonar.persistence.SonarServers.NO_SONAR;
import static org.intellij.sonar.persistence.SonarServers.PROJECT;
import static org.intellij.sonar.util.UIUtil.makeObj;

public class ModuleSonarServersView extends SonarServersView {

  private final ProjectSettings myProjectSettingsComponent;

  public ModuleSonarServersView(JComboBox mySonarServersComboBox, JButton myAddSonarServerButton, JButton myEditSonarServerButton, JButton myRemoveSonarServerButton, Project myProject) {
    super(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, myProject);
    myProjectSettingsComponent = myProject.getComponent(ProjectSettings.class);
  }

  @Override
  public String getSelectedItem() {
    final String selectedItem = super.getSelectedItem();
    if (SonarServers.PROJECT.equals(selectedItem)) {
//      final ProjectSettingsBean projectSettingsBean = myProjectSettingsComponent.getState();
      final ProjectSettingsBean projectSettingsBean = null;
      return null != projectSettingsBean ? projectSettingsBean.getSonarServerName() : NO_SONAR;
    }
    return selectedItem;
  }
/*
  public String getProperSonarServerName(String sonarServerName) {
    if (ModuleSettingsConfigurable.PROJECT.equals(sonarServerName)) {
      final ProjectSettingsBean moduleSettingsBean = myProjectSettingsComponent.getState();
      return null != moduleSettingsBean ? moduleSettingsBean.getSonarServerName() : ProjectSettingsConfigurable.NO_SONAR;
    }
  }*/

  @Override
  protected boolean editAndRemoveButtonsCanBeEnabled() {
    final boolean isNoSonarSelected = NO_SONAR.equals(mySonarServersComboBox.getSelectedItem().toString());
    final boolean isProjectSonarSelected = PROJECT.equals(mySonarServersComboBox.getSelectedItem().toString());
    return !isNoSonarSelected && !isProjectSonarSelected;
  }

  @Override
  protected void initSonarServersComboBox() {
    Optional<Collection<SonarServerConfig>> sonarServerConfigurationBeans = SonarServers.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(PROJECT));
      mySonarServersComboBox.addItem(makeObj(NO_SONAR));
      for (SonarServerConfig sonarServerConfigBean : sonarServerConfigurationBeans.get()) {
        mySonarServersComboBox.addItem(makeObj(sonarServerConfigBean.getName()));
      }
    }
  }
}
