package org.intellij.sonar.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.project.ProjectSettingsConfigurable;
import org.intellij.sonar.persistence.ProjectSettingsBean;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class LoadAllSonarProjectsWithModulesRunnable implements Runnable {

  private JList resourcesList;

  private Project project;

  private ProjectSettingsConfigurable projectSettingsConfigurable;

  public LoadAllSonarProjectsWithModulesRunnable(Project project, JList resourcesList, ProjectSettingsConfigurable projectSettingsConfigurable) {
    this.project = project;
    this.resourcesList = resourcesList;
    this.projectSettingsConfigurable = projectSettingsConfigurable;
  }

  @Override
  public void run() {
    try {
      ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      indicator.setText("Loading resources...");
      indicator.setFraction(0.0);
      loadAllSonarProjectsWithModules(indicator);
      indicator.setFraction(1.0);
    } catch (Exception e) {
      throw new ProcessCanceledException(e);
    }
  }

  private void loadAllSonarProjectsWithModules(@NotNull ProgressIndicator indicator) {

    ProjectSettingsBean projectSettingsBean = projectSettingsConfigurable.toProjectSettingsBean();
    if (projectSettingsBean.isEmpty()) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          Messages.showMessageDialog("You need to specify connection settings first", "Loading Failed", null);
        }
      });
    }
    SonarServer sonarServer = ServiceManager.getService(SonarServer.class);
//    Sonar sonar = projectSettingsBean.useAnonymous ?
//        sonarServer.createSonar(projectSettingsBean.sonarServerHostUrl, null, null) :
//        sonarServer.createSonar(projectSettingsBean.sonarServerHostUrl, projectSettingsBean.user, projectSettingsBean.password);
//    List<Resource> allProjectsWithModules = sonarServer.getAllProjectsAndModules(sonar);
//    if (null != allProjectsWithModules) {
//      if (null == projectSettingsBean.downloadedResources)
//        projectSettingsBean.downloadedResources = new ArrayList<SonarResourceBean>(allProjectsWithModules.size());
//      projectSettingsBean.downloadedResources.clear();
//      int projectCount = 0;
//      for (Resource projectOrModule : allProjectsWithModules) {
//        SonarResourceBean sonarResourceBean = new SonarResourceBean(projectOrModule);
//        projectSettingsBean.downloadedResources.add(sonarResourceBean);
//        indicator.checkCanceled();
//        projectCount++;
//        indicator.setFraction((double) projectCount / (double) allProjectsWithModules.size());
//        indicator.setText2(projectOrModule.getName());
//      }
//    }
//
//    ProjectSettingsComponent projectSettingsComponent = project.getComponent(ProjectSettingsComponent.class);
//    projectSettingsComponent.loadState(projectSettingsBean);
//
//    Collection<Object> resourcesListData = toResourcesListDataFrom(projectSettingsBean.downloadedResources);
//    moveLaterToResourcesList(resourcesListData);
  }

  public static Collection<Object> toResourcesListDataFrom(Collection<SonarResourceBean> sonarResourceBeans) {
    Collection<Object> resourcesListData = new ArrayList<Object>(sonarResourceBeans.size());
    for(SonarResourceBean sonarResourceBean: sonarResourceBeans) {
      if (sonarResourceBean.qualifier.equals(SonarQualifier.PROJECT)) {
        resourcesListData.add(sonarResourceBean.name + " (" + sonarResourceBean.key + ")");
      } else if (sonarResourceBean.qualifier.equals(SonarQualifier.MODULE)) {
        resourcesListData.add("          " + sonarResourceBean.name + " (" + sonarResourceBean.key + ")");
      }
    }
    return resourcesListData;
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
