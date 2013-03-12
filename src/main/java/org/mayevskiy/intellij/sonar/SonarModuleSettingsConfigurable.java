package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.mayevskiy.intellij.sonar.component.SonarComponent;
import org.mayevskiy.intellij.sonar.component.SonarModuleComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;

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

    private SonarModuleComponent sonarComponent;
    private SonarService sonarService;
    private Project project;

    public SonarModuleSettingsConfigurable(Module module) {
        this.project = module.getProject();
        this.sonarComponent = module.getComponent(SonarModuleComponent.class);
        this.sonarService = new SonarService();
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
    public SonarComponent getSonarComponent() {
        return sonarComponent;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public JPanel getjPanel() {
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
}
