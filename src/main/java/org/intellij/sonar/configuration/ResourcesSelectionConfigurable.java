package org.intellij.sonar.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.intellij.sonar.persistence.ModuleSettingsBean;
import org.intellij.sonar.persistence.ModuleSettingsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ResourcesSelectionConfigurable {

  private JList resourcesList;
  private JButton updateListButton;
  private JPanel rootJPanel;
  private JButton OKButton;
  private JButton cancelButton;

  public JButton getUpdateListButton() {
    return updateListButton;
  }

  public JPanel getRootJPanel() {
    return rootJPanel;
  }

  @Nullable
  public JComponent createComponent() {
    getUpdateListButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
//        ProgressManager.getInstance().runProcessWithProgressSynchronously(
//            new LoadAllSonarProjectsWithModulesRunnable(project, resourcesList, projectSettingsConfigurable),
//            "Loading sonar resources", true, getMyProject());
      }
    });

//    final ModuleSettingsComponent projectSettingsComponent = project.getComponent(ModuleSettingsComponent.class);
//    final ModuleSettingsBean persistedProjectSettingsBean = projectSettingsComponent.getState();
//    if (persistedProjectSettingsBean != null && persistedProjectSettingsBean.downloadedResources != null) {
//      this.resourcesList.setListData(LoadAllSonarProjectsWithModulesRunnable.toResourcesListDataFrom(persistedProjectSettingsBean.downloadedResources).toArray());
//    }

    resourcesList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent listSelectionEvent) {
//        ModuleSettingsComponent projectSettingsComponent = project.getComponent(ModuleSettingsComponent.class);
//        if (projectSettingsComponent != null && projectSettingsComponent.getState() != null && projectSettingsComponent.getState().downloadedResources != null) {
//          int[] selectedIndices = ((JList) (listSelectionEvent).getSource()).getSelectedIndices();
//          Collection<SonarResourceBean> selectedSonarResources = new ArrayList<SonarResourceBean>(selectedIndices.length);
//          for (int selectedIndex: selectedIndices) {
//            SonarResourceBean sonarResourceBean = Iterables.get(projectSettingsComponent.getState().downloadedResources, selectedIndex);
//            selectedSonarResources.add(sonarResourceBean);
//          }

//          Collection<Object> resourcesListData = LoadAllSonarProjectsWithModulesRunnable.toResourcesListDataFrom(selectedSonarResources);
//          projectSettingsConfigurable.getResourcesList().setListData(resourcesListData.toArray());
//        }
      }
    });

    return getRootJPanel();
  }

}
