package org.intellij.sonar;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:53
 */
public class SonarModuleSettingsConfigurable extends SonarSettingsConfigurable implements Configurable {

  private JButton testConnectionButton;
  private JPanel jPanel;
  private JTextField sonarServerUrlTextField;
  private JTextField sonarUserTextField;
  private JTextField sonarPasswordTextField;
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

  @Nullable
  //for backward compatibility
  public Icon getIcon() {
    return null;
  }
}
