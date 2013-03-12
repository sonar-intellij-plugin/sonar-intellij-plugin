package org.mayevskiy.intellij.sonar;

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
import org.mayevskiy.intellij.sonar.component.SonarComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

/**
 * Author: Oleg Mayevskiy
 * Date: 13.03.13
 * Time: 00:36
 */
public abstract class SonarSettingsConfigurable implements Configurable {
    private static final String DEFAULT_SERVER_URL = "";
    private static final String DEFAULT_USER = "";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_RESOURCE = "";
    public static String SONAR_SERVER_VERSION;

    public abstract JButton getTestConnectionButton();

    public abstract SonarService getSonarService();

    public abstract SonarComponent getSonarComponent();

    public abstract Project getProject();

    public abstract JPanel getjPanel();

    public abstract JTextField getSonarServerUrlTextField();

    public abstract JTextField getSonarUserTextField();

    public abstract JTextField getSonarPasswordTextField();

    public abstract JTextField getSonarResourceTextField();

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
        fromSettingsBean(getSonarComponent().getState());

        getTestConnectionButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean processed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
                        new TestConnectionRunnable(),
                        "Testing Connection", true, getProject()
                );

                if (processed) {
                    Messages.showMessageDialog(MessageFormat.format("Connection successful\nSonar v.{0}", SONAR_SERVER_VERSION), "Connection Test", Messages.getInformationIcon());
                } else {
                    Messages.showMessageDialog("Connection not successful", "Connection Test", Messages.getInformationIcon());
                }

            }
        });
        return getjPanel();
    }

    private void fromSettingsBean(SonarSettingsBean state) {
        if (null == state) {
            getSonarServerUrlTextField().setText(DEFAULT_SERVER_URL);
            getSonarUserTextField().setText(DEFAULT_USER);
            getSonarPasswordTextField().setText(DEFAULT_PASSWORD);
            getSonarResourceTextField().setText(DEFAULT_RESOURCE);
        } else {
            getSonarServerUrlTextField().setText(state.host);
            getSonarUserTextField().setText(state.user);
            getSonarPasswordTextField().setText(state.password);
            getSonarResourceTextField().setText(state.resource);
        }
    }

    private SonarSettingsBean toSettingsBean() {
        SonarSettingsBean result = new SonarSettingsBean();
        result.host = getSonarServerUrlTextField().getText();
        result.user = getSonarUserTextField().getText();
        result.password = getSonarPasswordTextField().getText();
        result.resource = getSonarResourceTextField().getText();
        return result;
    }

    @Override
    public boolean isModified() {
        SonarSettingsBean state = getSonarComponent().getState();
        return null == state || !state.equals(toSettingsBean());
    }

    @Override
    public void apply() throws ConfigurationException {
        getSonarComponent().loadState(toSettingsBean());
    }

    @Override
    public void reset() {
        fromSettingsBean(getSonarComponent().getState());
    }

    @Override
    public void disposeUIResources() {
    }

    private class TestConnectionRunnable implements Runnable {

        @Override
        public void run() {
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setText("Testing Connection");
            indicator.setText2(String.format("connecting to %s", getSonarServerUrlTextField().getText()));
            indicator.setFraction(0.5);
            indicator.setIndeterminate(true);

            try {
                SONAR_SERVER_VERSION = getSonarService().testConnectionByGettingSonarServerVersion(toSettingsBean());
            } catch (Exception re) {
                throw new ProcessCanceledException();
            }

            if (indicator.isCanceled()) {
                throw new ProcessCanceledException();
            }
        }
    }
}
