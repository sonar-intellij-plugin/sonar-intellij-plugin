package org.intellij.sonar.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ResourcesSelectionConfigurable extends DialogWrapper implements Configurable {
    private JList list1;
    private JButton updateListButton;

    protected ResourcesSelectionConfigurable(boolean canBeParent) {
        super(canBeParent);
        init();
    }

    public JPanel getRootJPanel() {
        return rootJPanel;
    }

    private JPanel rootJPanel;

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
