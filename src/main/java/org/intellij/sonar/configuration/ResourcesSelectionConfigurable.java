package org.intellij.sonar.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResourcesSelectionConfigurable extends DialogWrapper implements Configurable {

  private Project project;

  private JList resourcesList;

  public JButton getUpdateListButton() {
    return updateListButton;
  }

  private JButton updateListButton;

  public ResourcesSelectionConfigurable(@Nullable Project project, boolean canBeParent) {
    super(project, canBeParent);
    this.project = project;
    init();
  }

  public Project getProject() {
    return project;
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
    getUpdateListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {

        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            new LoadAllSonarProjectsWithModulesRunnable(project, resourcesList),
            "Loading sonar resources", true, getProject());
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
