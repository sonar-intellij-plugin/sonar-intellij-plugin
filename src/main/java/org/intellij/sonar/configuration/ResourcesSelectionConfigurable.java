package org.intellij.sonar.configuration;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ResourcesSelectionConfigurable extends DialogWrapper implements Configurable {

  private Project project;



  private JList resourcesList;

  public JList getResourcesList() {
    return resourcesList;
  }

  public JButton getUpdateListButton() {

    return updateListButton;
  }

  private JButton updateListButton;

  public ResourcesSelectionConfigurable(@Nullable Project project, boolean canBeParent) {
    super(project, canBeParent);
    this.project = project;
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
    getUpdateListButton().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        loadAllSonarProjectsWithModules();
      }
    });
    return getRootJPanel();
  }

  private void loadAllSonarProjectsWithModules() {

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
        for (Resource projectOrModule : allProjectsWithModules) {
          if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_PROJECT)) {
            resourcesListData.add(projectOrModule.getName() + " ("+projectOrModule.getKey()+")");
          } else if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
            resourcesListData.add("          "+projectOrModule.getName() + " ("+projectOrModule.getKey()+")");
          }
        }
      }
    }

    getResourcesList().setListData(resourcesListData.toArray());
    //To change body of created methods use File | Settings | File Templates.
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
