package org.intellij.sonar.configuration;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogWrapper;

public class LoadConfigurationConfigurable extends DialogWrapper {
    private JComboBox projectOrModule;

    public LoadConfigurationConfigurable(boolean canBeParent) {
        super(canBeParent);
        init();
    }

    public JCheckBox getLoadFromProjectModuleCheckBox() {
        return loadFromProjectModuleCheckBox;
    }

    @Override
    protected void doOKAction() {
        if (getOKAction().isEnabled()) {
            close(OK_EXIT_CODE);
        }
    }

    @Override
    public void doCancelAction() {
        if (getCancelAction().isEnabled()) {
            close(CANCEL_EXIT_CODE);
        }
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

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
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
}
