package org.intellij.sonar.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.NotNull;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LoadAllSonarProjectsWithModulesRunnable implements Runnable {

  private JList resourcesList;

  private Project project;

  public LoadAllSonarProjectsWithModulesRunnable(Project project, JList resourcesList) {
    this.project = project;
    this.resourcesList = resourcesList;
  }

  @Override
  public void run() {
    ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    indicator.setText("Loading resources...");
    indicator.setFraction(0.0);
    loadAllSonarProjectsWithModules(indicator);
    indicator.setFraction(1.0);
  }

  private void loadAllSonarProjectsWithModules(@NotNull ProgressIndicator indicator) {

    Collection<Object> resourcesListData = new LinkedList<Object>();

    ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
    ProjectSettingsBean projectSettingsBean = projectSettingsComponent.getState();
    if (null != projectSettingsBean) {
      SonarService sonarService = ServiceManager.getService(SonarService.class);
      Sonar sonar = projectSettingsBean.useAnonymous ?
          sonarService.createSonar(projectSettingsBean.sonarServerHostUrl, null, null) :
          sonarService.createSonar(projectSettingsBean.sonarServerHostUrl, projectSettingsBean.user, projectSettingsBean.password);
      List<Resource> allProjectsWithModules = sonarService.getAllProjectsWithModules(sonar);
      if (null != allProjectsWithModules) {
        int projectCount = 0;
        for (Resource projectOrModule : allProjectsWithModules) {
          indicator.checkCanceled();
          projectCount++;
          indicator.setFraction((double) projectCount / (double) allProjectsWithModules.size());
          indicator.setText2(projectOrModule.getName());
          if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_PROJECT)) {
            resourcesListData.add(projectOrModule.getName() + " (" + projectOrModule.getKey() + ")");
          } else if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
            resourcesListData.add("          " + projectOrModule.getName() + " (" + projectOrModule.getKey() + ")");
          }
        }
      }
    }

    moveLaterToResourcesList(resourcesListData);
  }

  private void moveLaterToResourcesList(Collection<Object> resourcesListData) {
    final Object[] resourcesListDataArray = resourcesListData.toArray();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        resourcesList.setListData(resourcesListDataArray);
      }
    });
  }
}
