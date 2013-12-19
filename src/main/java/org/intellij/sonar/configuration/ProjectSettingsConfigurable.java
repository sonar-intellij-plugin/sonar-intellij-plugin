package org.intellij.sonar.configuration;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

    private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);

    private Project project;

    private JCheckBox shareConfigurationMakesVisibleCheckBox;

    private JButton testConfigurationButton;

    private JList resourcesList;

    public JButton getAddResourcesButton() {
        return addResourcesButton;
    }

    public ProjectSettingsConfigurable(Project project) {
        this.project = project;
    }

    private JButton addResourcesButton;

    private JCheckBox useAnonymousCheckBox;

    private JTextField sonarServerTextField;

    public Project getProject() {
        return project;
    }

    public JList getResourcesList() {
        return resourcesList;
    }

    public JCheckBox getShareConfigurationMakesVisibleCheckBox() {
        return shareConfigurationMakesVisibleCheckBox;
    }

    public JTextField getSonarServerTextField() {
        return sonarServerTextField;
    }

    private JTextField userTextField;

    private JPasswordField passwordField;

    public JButton getLoadConfigurationButton() {
        return loadConfigurationButton;
    }

    private JButton loadConfigurationButton;

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JTextField getUserTextField() {
        return userTextField;
    }

    public JPanel getRootJPanel() {
        getLoadConfigurationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new LoadConfigurationConfigurable(true).show();
            }
        });

        getAddResourcesButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new ResourcesSelectionConfigurable(true).show();
            }
        });

        return rootJPanel;
    }

    private JPanel rootJPanel;

    public JCheckBox getShowPasswordCheckBox() {
        return showPasswordCheckBox;
    }

    private JCheckBox showPasswordCheckBox;

    @Nls
    @Override
    public String getDisplayName() {
        return "SonarQube";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    public JCheckBox getUseAnonymousCheckBox() {
        return useAnonymousCheckBox;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
        if (null != projectSettingsComponent) {
            ProjectSettingsBean projectSettingsBean = projectSettingsComponent.getState();
            ProjectSettingsConfigurableBeanConverter.convertFromBean(projectSettingsBean, this);
            setCredentialsEnabled();
        }

        getUseAnonymousCheckBox().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                setCredentialsEnabled();
            }
        });

        getShowPasswordCheckBox().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                if (getShowPasswordCheckBox().isSelected()) {
                    makePasswordVisible();
                } else {
                    makePasswordInvisible();
                }
            }
        });

        return getRootJPanel();
    }

    private void makePasswordInvisible() {
        getPasswordField().setEchoChar('•');
    }

    private void makePasswordVisible() {
        getPasswordField().setEchoChar('\u0000');
    }

    private void setCredentialsEnabled() {
        getUserTextField().setEnabled(!getUseAnonymousCheckBox().isSelected());
        getPasswordField().setEnabled(!getUseAnonymousCheckBox().isSelected());
    }

    @Override
    public boolean isModified() {
        ProjectSettingsBean state = project.getComponent(ProjectSettingsComponent.class).getState();
        return null == state || !state.equals(ProjectSettingsConfigurableBeanConverter.convertToBean(this));
    }

    @Override
    public void apply() throws ConfigurationException {
        ProjectSettingsBean projectSettingsBean = ProjectSettingsConfigurableBeanConverter.convertToBean(this);
        ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
        projectSettingsComponent.loadState(projectSettingsBean);
        PasswordManager.storePassword(project, projectSettingsBean);
    }

    @Override
    public void reset() {
        ProjectSettingsBean persistedState = project.getComponent(ProjectSettingsComponent.class).getState();
        ProjectSettingsConfigurableBeanConverter.convertFromBean(persistedState, this);
    }

    @Override
    public void disposeUIResources() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectOpened() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectClosed() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initComponent() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeComponent() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "SonarQube";
    }

}
