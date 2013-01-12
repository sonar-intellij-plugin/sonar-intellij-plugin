package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SonarProjectSettingsConfigurable implements Configurable {

    private JTextField sonarServerUrlTextField;
    private JButton testConnectionButton;
    private JPanel jPanel;
    private SonarProjectComponent sonarProjectComponent;
    private SonarService sonarService = new SonarService();

    public SonarProjectSettingsConfigurable(Project project) {
        this.sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
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
        sonarServerUrlTextField.setText(sonarProjectComponent.getState().host);

        testConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean connectionSuccessful = sonarService.testConnection(sonarServerUrlTextField.getText());
                if (connectionSuccessful) {
                    Messages.showMessageDialog("Connection successful", "Connection test", Messages.getInformationIcon());
                } else {
                    String msg = String.format("Connection to %s not successful", sonarServerUrlTextField.getText());
                    Messages.showMessageDialog(msg, "Connection test", Messages.getInformationIcon());
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
