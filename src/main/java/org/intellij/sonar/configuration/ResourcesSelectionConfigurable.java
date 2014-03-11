package org.intellij.sonar.configuration;

import com.google.common.collect.Iterables;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.sonar.configuration.project.ProjectSettingsBean;
import org.intellij.sonar.configuration.project.ProjectSettingsComponent;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

public class ResourcesSelectionConfigurable extends DialogWrapper implements Configurable {

  private Project project;

  private JList resourcesList;
  private JButton updateListButton;
  private JPanel rootJPanel;
  private ProjectSettingsConfigurable projectSettingsConfigurable;

  public ResourcesSelectionConfigurable(@Nullable Project project, boolean canBeParent, ProjectSettingsConfigurable projectSettingsConfigurable) {
    super(project, canBeParent);
    this.project = project;
    this.projectSettingsConfigurable = projectSettingsConfigurable;
    init();
  }

  public JButton getUpdateListButton() {
    return updateListButton;
  }

  public Project getProject() {
    return project;
  }

  public JPanel getRootJPanel() {
    return rootJPanel;
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
    getUpdateListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            new LoadAllSonarProjectsWithModulesRunnable(project, resourcesList, projectSettingsConfigurable),
            "Loading sonar resources", true, getProject());
      }
    });

    final ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
    final ProjectSettingsBean persistedProjectSettingsBean = projectSettingsComponent.getState();
    if (persistedProjectSettingsBean != null && persistedProjectSettingsBean.downloadedResources != null) {
      this.resourcesList.setListData(LoadAllSonarProjectsWithModulesRunnable.toResourcesListDataFrom(persistedProjectSettingsBean.downloadedResources).toArray());
    }

    resourcesList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
        ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
        if (projectSettingsComponent != null && projectSettingsComponent.getState() != null && projectSettingsComponent.getState().downloadedResources != null) {
          int[] selectedIndices = ((JList) (listSelectionEvent).getSource()).getSelectedIndices();
          Collection<SonarResourceBean> selectedSonarResources = new ArrayList<SonarResourceBean>(selectedIndices.length);
          for (int selectedIndex: selectedIndices) {
            SonarResourceBean sonarResourceBean = Iterables.get(projectSettingsComponent.getState().downloadedResources, selectedIndex);
            selectedSonarResources.add(sonarResourceBean);
          }

          Collection<Object> resourcesListData = LoadAllSonarProjectsWithModulesRunnable.toResourcesListDataFrom(selectedSonarResources);
          projectSettingsConfigurable.getResourcesList().setListData(resourcesListData.toArray());
        }
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
