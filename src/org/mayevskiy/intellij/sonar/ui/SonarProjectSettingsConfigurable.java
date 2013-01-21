package org.mayevskiy.intellij.sonar.ui;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.component.SonarProjectComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SonarProjectSettingsConfigurable implements Configurable {


    private JButton testConnectionButton;
    private JPanel jPanel;
    private JTextField sonarServerUrlTextField;
    private JTextField sonarUserTextField;
    private JTextField sonarPasswordTextField;
    private JTextField sonarResourceTextField;

    private SonarProjectComponent sonarProjectComponent;
    private SonarService sonarService;
    private Project project;

    public SonarProjectSettingsConfigurable(Project project) {
        this.project = project;
        this.sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
        this.sonarService = new SonarService();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Sonar";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        SonarSettingsBean state = this.sonarProjectComponent.getState();
        if (null != state) {
            this.sonarServerUrlTextField.setText(state.host);
            this.sonarUserTextField.setText(state.user);
            this.sonarPasswordTextField.setText(state.password);
            this.sonarResourceTextField.setText(state.resource);
        }

        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean processed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                        new Runnable() {
                            @Override
                            public void run() {
                                ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                                indicator.setText("Testing Connection");
                                indicator.setText2(String.format("connecting to %s", sonarServerUrlTextField.getText()));
                                indicator.setFraction(0.5);
                                indicator.setIndeterminate(true);

                                try {
                                    sonarService.testConnection(new SonarSettingsBean(sonarServerUrlTextField.getText(), sonarUserTextField.getText(), sonarPasswordTextField.getText(), sonarResourceTextField.getText()));
                                } catch (RuntimeException re) {
                                    throw new ProcessCanceledException();
                                }

                                if (indicator.isCanceled()) {
                                    throw new ProcessCanceledException();
                                }

                            }
                        },
                        "Testing Connection", true, project
                );

                if (processed) {
                    Messages.showMessageDialog("Connection successful", "Connection Test", Messages.getInformationIcon());
                } else {
                    Messages.showMessageDialog("Connection not successful", "Connection Test", Messages.getInformationIcon());
                }

            }
        });
        return jPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        SonarSettingsBean state = sonarProjectComponent.getState();
        if (null != state) {
            state.host = sonarServerUrlTextField.getText();
            state.user = sonarUserTextField.getText();
            state.password = sonarPasswordTextField.getText();
            state.resource = sonarResourceTextField.getText();
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }

}
