package org.intellij.sonar.configuration.partials;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.module.ModuleSettingsConfigurable;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.intellij.sonar.persistence.ProjectSettingsBean;
import org.intellij.sonar.persistence.ProjectSettingsComponent;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;

import javax.swing.*;

import java.util.Collection;

import static org.intellij.sonar.configuration.module.ModuleSettingsConfigurable.PROJECT_SONAR;
import static org.intellij.sonar.configuration.project.ProjectSettingsConfigurable.NO_SONAR;

public class ModuleSonarServersView extends SonarServersView {

  private final ProjectSettingsComponent myProjectSettingsComponent;

  public ModuleSonarServersView(JComboBox mySonarServersComboBox, JButton myAddSonarServerButton, JButton myEditSonarServerButton, JButton myRemoveSonarServerButton, Project myProject) {
    super(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, myProject);
    myProjectSettingsComponent = myProject.getComponent(ProjectSettingsComponent.class);
  }

  @Override
  public String getSelectedItem() {
    final String selectedItem = super.getSelectedItem();
    if (ModuleSettingsConfigurable.PROJECT_SONAR.equals(selectedItem)) {
      final ProjectSettingsBean projectSettingsBean = myProjectSettingsComponent.getState();
      return null != projectSettingsBean ? projectSettingsBean.getSonarServerName() : ProjectSettingsConfigurable.NO_SONAR;
    }
    return selectedItem;
  }

  @Override
  protected boolean editAndRemoveButtonsCanBeEnabled() {
    final boolean isNoSonarSelected = NO_SONAR.equals(mySonarServersComboBox.getSelectedItem().toString());
    final boolean isProjectSonarSelected = PROJECT_SONAR.equals(mySonarServersComboBox.getSelectedItem().toString());
    return !isNoSonarSelected && !isProjectSonarSelected;
  }

  @Override
  protected void initSonarServersComboBox() {
    Optional<Collection<SonarServerConfigurationBean>> sonarServerConfigurationBeans = SonarServersService.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(PROJECT_SONAR));
      mySonarServersComboBox.addItem(makeObj(NO_SONAR));
      for (SonarServerConfigurationBean sonarServerConfigurationBean : sonarServerConfigurationBeans.get()) {
        mySonarServersComboBox.addItem(makeObj(sonarServerConfigurationBean.getName()));
      }
    }
  }
}
