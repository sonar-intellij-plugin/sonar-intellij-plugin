package org.mayevskiy.intellij.sonar.ui;

import com.intellij.codeInsight.completion.CompletionProgressIndicator;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.sun.deploy.util.Waiter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.component.SonarProjectComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SonarProjectSettingsConfigurable implements Configurable {

    private JTextField sonarServerUrlTextField;
    private JButton testConnectionButton;
    private JPanel jPanel;
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
        //noinspection ConstantConditions
        this.sonarServerUrlTextField.setText(this.sonarProjectComponent.getState().host);

        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean processed= ProgressManager.getInstance().runProcessWithProgressSynchronously(
                        new Runnable() {
                            @Override
                            public void run() {
                                ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                                indicator.setText("Testing Connection");
                                indicator.setText2(String.format("connecting to %s", sonarServerUrlTextField.getText()));
                                indicator.setFraction(0.5);
                                indicator.setIndeterminate(true);

                                try {
                                    sonarService.testConnection(sonarServerUrlTextField.getText());
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




/*

                new Task.Backgroundable(project, "Testing Connection", true) {
                    @Override
                    public boolean shouldStartInBackground() {
                        return false;
                    }

                    private boolean connectionSuccessful;

                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setText("Testing Connection");
                        indicator.setText2(String.format("connecting to %s", sonarServerUrlTextField.getText()));
                        indicator.setFraction(0.5);
                        indicator.setIndeterminate(true);
                        try {
                            connectionSuccessful = sonarService.testConnection(sonarServerUrlTextField.getText());
                        } catch (Exception e1) {
                            connectionSuccessful = false;
                        }

                    }

                    @Override
                    public void onSuccess() {
                        if (connectionSuccessful) {
                            Messages.showMessageDialog("Connection successful", "Connection Test", Messages.getInformationIcon());
                        } else {
                            Messages.showMessageDialog("Connection not successful", "Connection Test", Messages.getInformationIcon());
                        }
                    }
                }.queue();

*/

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
        //noinspection ConstantConditions
        sonarProjectComponent.getState().host = sonarServerUrlTextField.getText();
    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
    }

}
