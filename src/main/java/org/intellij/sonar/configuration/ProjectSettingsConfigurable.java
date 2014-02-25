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
import java.util.ArrayList;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

  private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);

  private Project project;

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

  public JTextField getSonarServerTextField() {
    return sonarServerTextField;
  }

  private JTextField userTextField;

  private JPasswordField passwordField;

  public JPasswordField getPasswordField() {
    return passwordField;
  }

  public JTextField getUserTextField() {
    return userTextField;
  }

  public JPanel getRootJPanel() {
    final ProjectSettingsConfigurable projectSettingsConfigurable = this;
    getAddResourcesButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        new ResourcesSelectionConfigurable(project, true, projectSettingsConfigurable).show();
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
      this.setValuesFromProjectSettingsBean(projectSettingsBean);
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
    return null == state || !state.equals(this.toProjectSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    ProjectSettingsBean projectSettingsBean = this.toProjectSettingsBean();
    ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
    projectSettingsComponent.loadState(projectSettingsBean);
    PasswordManager.storePassword(project, projectSettingsBean);
  }

  @Override
  public void reset() {
    ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
    if (projectSettingsComponent != null && projectSettingsComponent.getState() != null) {
      ProjectSettingsBean persistedState = projectSettingsComponent.getState();
      this.setValuesFromProjectSettingsBean(persistedState);
    }
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

  public ProjectSettingsBean toProjectSettingsBean() {

    ProjectSettingsBean projectSettingsBean = new ProjectSettingsBean();
    projectSettingsBean.sonarServerHostUrl = this.getSonarServerTextField().getText();
    projectSettingsBean.useAnonymous = this.getUseAnonymousCheckBox().isSelected();
    projectSettingsBean.user = this.getUserTextField().getText();
    projectSettingsBean.password = String.valueOf(this.getPasswordField().getPassword());

    ProjectSettingsBean persistedProjectSettingsBean = project.getComponent(ProjectSettingsComponent.class).getState();
    if (persistedProjectSettingsBean != null) {
      projectSettingsBean.downloadedResources = persistedProjectSettingsBean.downloadedResources;
    }
    convertResourcesListToBean(projectSettingsBean);

    return projectSettingsBean;
  }

  private void convertResourcesListToBean(ProjectSettingsBean projectSettingsBean) {
    ListModel model = this.getResourcesList().getModel();
    projectSettingsBean.resources = new ArrayList<String>(model.getSize());
    for (int i = 0; i < model.getSize(); i++) {
      projectSettingsBean.resources.add((String) model.getElementAt(i));
    }
  }

  public void setValuesFromProjectSettingsBean(ProjectSettingsBean projectSettingsBean) {
    if (null == projectSettingsBean) return;

    this.getSonarServerTextField().setText(projectSettingsBean.sonarServerHostUrl);
    this.getUseAnonymousCheckBox().setSelected(projectSettingsBean.useAnonymous);
    this.getUserTextField().setText(projectSettingsBean.user);
    this.getPasswordField().setText(projectSettingsBean.password);
    this.getResourcesList().setListData(projectSettingsBean.resources.toArray());
  }

}
