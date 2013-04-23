package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.project.Project;

import javax.swing.*;

public class SonarProjectSettingsConfigurable extends SonarSettingsConfigurable {

    private JButton testConnectionButton;
    private JPanel jPanel;
    private JTextField sonarServerUrlTextField;
    private JTextField sonarUserTextField;
    private JTextField sonarPasswordTextField;
    private JTextField sonarResourceTextField;

    private SonarSettingsComponent sonarSettingsComponent;
    private SonarService sonarService;
    private Project project;

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
        return sonarSettingsComponent;
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

    public SonarProjectSettingsConfigurable(Project project) {
        this.project = project;
        this.sonarSettingsComponent = project.getComponent(SonarSettingsProjectComponent.class);
        this.sonarService = new SonarService();
    }


}
