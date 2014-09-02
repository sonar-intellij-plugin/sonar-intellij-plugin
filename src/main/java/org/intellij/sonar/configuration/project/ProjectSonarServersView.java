package org.intellij.sonar.configuration.project;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.partials.SonarServersView;
import org.intellij.sonar.persistence.SonarServerConfiguration;
import org.intellij.sonar.persistence.SonarServers;

import javax.swing.*;
import java.util.Collection;

import static org.intellij.sonar.persistence.SonarServers.NO_SONAR;
import static org.intellij.sonar.util.UIUtil.makeObj;

public class ProjectSonarServersView extends SonarServersView {
  public ProjectSonarServersView(JComboBox mySonarServersComboBox, JButton myAddSonarServerButton, JButton myEditSonarServerButton, JButton myRemoveSonarServerButton, Project myProject) {
    super(mySonarServersComboBox, myAddSonarServerButton, myEditSonarServerButton, myRemoveSonarServerButton, myProject);
  }

  @Override
  protected boolean editAndRemoveButtonsCanBeEnabled() {
    return !NO_SONAR.equals(mySonarServersComboBox.getSelectedItem().toString());
  }

  @Override
  protected void initSonarServersComboBox() {
    Optional<Collection<SonarServerConfiguration>> sonarServerConfigurationBeans = SonarServers.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(NO_SONAR));
      for (SonarServerConfiguration sonarServerConfigurationBean : sonarServerConfigurationBeans.get()) {
        mySonarServersComboBox.addItem(makeObj(sonarServerConfigurationBean.getName()));
      }
    }
  }
}
