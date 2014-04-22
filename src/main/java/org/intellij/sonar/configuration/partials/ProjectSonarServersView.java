package org.intellij.sonar.configuration.partials;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;

import javax.swing.*;

import java.util.Collection;

import static org.intellij.sonar.configuration.project.ProjectSettingsConfigurable.NO_SONAR;

public class ProjectSonarServersView extends SonarServersView {
  public ProjectSonarServersView(JComboBox mySonarServersComboBox, JButton myAddSonarServerButton, JButton myEditSonarServerButton, JButton myRemoveSonarServerButton, Project myProject) {
    super(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, myProject);
  }

  @Override
  boolean editAndRemoveButtonsCanBeEnabled() {
    return NO_SONAR.equals(mySonarServersComboBox.getSelectedItem().toString());
  }

  @Override
  protected void initSonarServersComboBox() {
    Optional<Collection<SonarServerConfigurationBean>> sonarServerConfigurationBeans = SonarServersService.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(NO_SONAR));
      for (SonarServerConfigurationBean sonarServerConfigurationBean : sonarServerConfigurationBeans.get()) {
        mySonarServersComboBox.addItem(makeObj(sonarServerConfigurationBean.getName()));
      }
    }
  }
}
