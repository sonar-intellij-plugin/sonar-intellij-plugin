package org.intellij.sonar.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LoadConfiguration extends DialogWrapper implements Configurable {
    private JComboBox projectOrModule;

    public LoadConfiguration(boolean canBeParent) {
        super(canBeParent);
        init();
    }

    public JCheckBox getLoadFromProjectModuleCheckBox() {
        return loadFromProjectModuleCheckBox;
    }

    private JCheckBox loadFromProjectModuleCheckBox;
    private JTextField configurationFilePath;
    private JButton selectFileButton;
    private JPanel rootJPanel;

    public JTextField getConfigurationFilePath() {
        return configurationFilePath;
    }

    public JPanel getRootJPanel() {
        return rootJPanel;
    }

    public JButton getSelectFileButton() {
        return selectFileButton;
    }

    public JComboBox getProjectOrModule() {
        return projectOrModule;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        getLoadFromProjectModuleCheckBox().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                getProjectOrModule().setEnabled(getLoadFromProjectModuleCheckBox().isSelected());
                getConfigurationFilePath().setEnabled(!getLoadFromProjectModuleCheckBox().isSelected());
                getSelectFileButton().setEnabled(!getLoadFromProjectModuleCheckBox().isSelected());
            }
        });
        return getRootJPanel();
    }

    @Override
    public boolean isModified() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void apply() throws ConfigurationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return createComponent();
    }
}
