package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.module.Module;
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
import org.mayevskiy.intellij.sonar.component.SonarModuleComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:53
 */
public class SonarModuleSettingsConfigurable implements Configurable {

    private static final String DEFAULT_SERVER_URL = "";
    private static final String DEFAULT_USER = "";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_RESOURCE = "";

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

    @Nls
    @Override
    public String getDisplayName() {
        return "Sonar Connector";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        fromSettingsBean(sonarComponent.getState());

        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean processed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                        new TestConnectionRunnable(),
                        "Testing Connection", true, project
                );

                if (processed) {
                    Messages.showMessageDialog(MessageFormat.format("Connection successful\nSonar v.{0}", SONAR_SERVER_VERSION), "Connection Test", Messages.getInformationIcon());
                } else {
                    Messages.showMessageDialog("Connection not successful", "Connection Test", Messages.getInformationIcon());
                }

            }
        });
        return jPanel;
    }

    public static String SONAR_SERVER_VERSION;

    private class TestConnectionRunnable implements Runnable {

        @Override
        public void run() {
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setText("Testing Connection");
            indicator.setText2(String.format("connecting to %s", sonarServerUrlTextField.getText()));
            indicator.setFraction(0.5);
            indicator.setIndeterminate(true);

            try {
                SONAR_SERVER_VERSION = sonarService.testConnectionByGettingSonarServerVersion(toSettingsBean());
            } catch (Exception re) {
                throw new ProcessCanceledException();
            }

            if (indicator.isCanceled()) {
                throw new ProcessCanceledException();
            }
        }
    }

    private void fromSettingsBean(SonarSettingsBean state) {
        if (null == state) {
            sonarServerUrlTextField.setText(DEFAULT_SERVER_URL);
            sonarUserTextField.setText(DEFAULT_USER);
            sonarPasswordTextField.setText(DEFAULT_PASSWORD);
            sonarResourceTextField.setText(DEFAULT_RESOURCE);
        } else {
            sonarServerUrlTextField.setText(state.host);
            sonarUserTextField.setText(state.user);
            sonarPasswordTextField.setText(state.password);
            sonarResourceTextField.setText(state.resource);
        }
    }

    private SonarSettingsBean toSettingsBean() {
        SonarSettingsBean result = new SonarSettingsBean();
        result.host = sonarServerUrlTextField.getText();
        result.user = sonarUserTextField.getText();
        result.password = sonarPasswordTextField.getText();
        result.resource = sonarResourceTextField.getText();
        return result;
    }

    @Override
    public boolean isModified() {
        SonarSettingsBean state = sonarComponent.getState();
        return null == state || !state.equals(toSettingsBean());
    }

    @Override
    public void apply() throws ConfigurationException {
        sonarComponent.loadState(toSettingsBean());
    }

    @Override
    public void reset() {
        fromSettingsBean(sonarComponent.getState());
    }

    @Override
    public void disposeUIResources() {
    }
}
