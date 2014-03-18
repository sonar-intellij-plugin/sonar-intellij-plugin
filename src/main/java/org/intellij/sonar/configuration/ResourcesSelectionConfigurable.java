package org.intellij.sonar.configuration;

import com.google.common.base.Optional;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ResourcesSelectionConfigurable extends DialogWrapper {

  public ResourcesSelectionConfigurable(@Nullable Project project, @NotNull String sonarServerName) {
    super(project);
    init();
    myProject = project;
    mySonarServerName = sonarServerName;
    mySelectSonarResourcesFrom.setText(mySelectSonarResourcesFrom.getText() + " " + mySonarServerName);
  }

  private Project myProject;
  private String mySonarServerName;
  private TableView<String> myResourcesTable = new TableView<String>();
  private JButton myDownloadResourcesButton;
  private JPanel myRootJPanel;
  private JLabel mySelectSonarResourcesFrom;
  private JPanel myPanelForSonarResources;

  /*@Nullable
  public JComponent createComponent() {
    getMyDownloadResourcesButton().addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
//        ProgressManager.getInstance().runProcessWithProgressSynchronously(
//            new LoadAllSonarProjectsWithModulesRunnable(project, myResourcesTable, projectSettingsConfigurable),
//            "Loading sonar resources", true, getMyProject());
      }
    });

//    final ModuleSettingsComponent projectSettingsComponent = project.getComponent(ModuleSettingsComponent.class);
//    final ModuleSettingsBean persistedProjectSettingsBean = projectSettingsComponent.getState();
//    if (persistedProjectSettingsBean != null && persistedProjectSettingsBean.downloadedResources != null) {
//      this.myResourcesTable.setListData(LoadAllSonarProjectsWithModulesRunnable.toResourcesListDataFrom(persistedProjectSettingsBean.downloadedResources).toArray());
//    }

    myResourcesTable.addListSelectionListener(new ListSelectionListener() {
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

    return myRootJPanel;
  }*/

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanelForSonarResources.setLayout(new BorderLayout());
    myPanelForSonarResources.add(createResourcesTableComponent(), BorderLayout.CENTER);
    new TableSpeedSearch(myResourcesTable);

    myDownloadResourcesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        DownloadResourcesRunnable downloadResourcesRunnable = new DownloadResourcesRunnable();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(downloadResourcesRunnable, "Loading sonar resources from server", true, myProject);
        // download resources for mySonarServerName
        // show resources in myResourcesTable
      }
    });

    return myRootJPanel;
  }

  private class DownloadResourcesRunnable implements Runnable {

    @Override
    public void run() {
      final Optional<SonarServerConfigurationBean> sonarServerConfiguration = SonarServersService.get(mySonarServerName);
      if (sonarServerConfiguration.isPresent()) {
        final Sonar sonar = SonarServer.getInstance().createSonar(sonarServerConfiguration.get());
        final List<Resource> allProjectsAndModules = SonarServer.getInstance().getAllProjectsAndModules(sonar);
      }
    }
  }

  private JComponent createResourcesTableComponent() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myResourcesTable, null).
        disableUpDownActions().
        disableAddAction().disableRemoveAction().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 400));
    return panelForTable;
  }
}
