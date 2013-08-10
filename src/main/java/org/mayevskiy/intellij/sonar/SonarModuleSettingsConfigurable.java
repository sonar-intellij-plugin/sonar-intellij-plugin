package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.sonarserver.SonarService;

import javax.swing.*;

/**
 * @author Oleg Mayevskiy
 * @author Michail Plushnikov
 */
public class SonarModuleSettingsConfigurable extends SonarSettingsConfigurable implements Configurable {

  private JButton testConnectionButton;
  private JPanel jPanel;
  private JTextField sonarServerUrlTextField;
  private JTextField sonarUserTextField;
  private JTextField sonarPasswordTextField;
  private JComboBox<String> sonarResourceComboBox;
  private JButton resourcesUpdateButton;
  private JTextField sonarResourceTextField;

  private SonarSettingsModuleComponent sonarComponent;
  private SonarService sonarService;
  private Project project;

  public SonarModuleSettingsConfigurable(Module module) {
    this.project = module.getProject();
    this.sonarComponent = module.getComponent(SonarSettingsModuleComponent.class);
    this.sonarService = ServiceManager.getService(SonarService.class);
  }


  @Override
  public JButton getTestConnectionButton() {
    return testConnectionButton;
  }

  @Override
  public JButton getUpdateResourcesButton() {
    return resourcesUpdateButton;
  }

  @Override
  public SonarService getSonarService() {
    return sonarService;
  }

  @Override
  public SonarSettingsComponent getSonarSettingsComponent() {
    return sonarComponent;
  }

  @Override
  public Project getProject() {
    return project;
  }

  @Override
  public JPanel getJPanel() {
    return jPanel;
  }

  @Override
  public JTextField getSonarServerUrlTextField() {
    return sonarServerUrlTextField;
  }

  @Override
  public JTextField getSonarUserTextField() {
    return sonarUserTextField;
  }

  @Override
  public JTextField getSonarPasswordTextField() {
    return sonarPasswordTextField;
  }

  @Override
  public JTextField getSonarResourceTextField() {
    return sonarResourceTextField;
  }

  @Override
  public JComboBox<String> getSonarResourceComboBox() {
    return sonarResourceComboBox;
  }

  @Nullable
  //for backward compatibility
  public Icon getIcon() {
    return null;
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
