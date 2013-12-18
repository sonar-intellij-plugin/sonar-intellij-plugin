package org.intellij.sonar.configuration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SonarProjectSettings  implements Configurable, ProjectComponent {
    private JCheckBox shareConfigurationCheckBox;

    private JButton testConfigurationButton;

    private JList resources;

    public JButton getAddResourcesButton() {
        return addResourcesButton;
    }

    private JButton addResourcesButton;

    private JCheckBox useAnonymousCheckBox;

    private JTextField sonarServer;

    private JTextField user;

    private JPasswordField password;

    public JButton getLoadConfigurationButton() {
        return loadConfigurationButton;
    }

    private JButton loadConfigurationButton;

    public JPasswordField getPassword() {
        return password;
    }

    public JTextField getUser() {
        return user;
    }

    public JPanel getRootJPanel() {
        getLoadConfigurationButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new LoadConfiguration(true).show();
            }
        });

        getAddResourcesButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new ResourcesSelection(true).show();
            }
        });

        return rootJPanel;
    }

    private JPanel rootJPanel;

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

        getUseAnonymousCheckBox().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                getUser().setEnabled(!getUseAnonymousCheckBox().isSelected());
                getPassword().setEnabled(!getUseAnonymousCheckBox().isSelected());
            }
        });

        return getRootJPanel();
    }

    @Override
    public boolean isModified() {
        return false; // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void apply() throws ConfigurationException {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reset() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeUIResources() {
        // To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectOpened() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectClosed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
