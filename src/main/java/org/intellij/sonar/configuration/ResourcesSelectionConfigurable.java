package org.intellij.sonar.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.intellij.sonar.persistence.SonarResource;
import org.intellij.sonar.persistence.SonarResourcesService;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ResourcesSelectionConfigurable extends DialogWrapper {

  private static final ColumnInfo<SonarResource, String> NAME_COLUMN = new ColumnInfo<SonarResource, String>("Name") {
    @Nullable
    @Override
    public String valueOf(SonarResource sonarResource) {
      return sonarResource.getName();
    }

    @Override
    public int getWidth(JTable table) {
      return 300;
    }
  };
  private static final ColumnInfo<SonarResource, String> KEY_COLUMN = new ColumnInfo<SonarResource, String>("Key") {
    @Nullable
    @Override
    public String valueOf(SonarResource sonarResource) {
      return sonarResource.getKey();
    }
  };
  private Project myProject;
  private String mySonarServerName;
  private TableView<SonarResource> myResourcesTable = new TableView<SonarResource>();
  private JButton myDownloadResourcesButton;
  private JPanel myRootJPanel;
  private JLabel mySelectSonarResourcesFrom;
  private JPanel myPanelForSonarResources;
  private List<Resource> allProjectsAndModules;

  public List<Resource> getSelectedSonarResources() {
    return selectedSonarResources;
  }

  private List<Resource> selectedSonarResources;

  public ResourcesSelectionConfigurable(@Nullable Project project, @NotNull String sonarServerName) {
    super(project);
    myProject = project;
    mySonarServerName = sonarServerName;
    mySelectSonarResourcesFrom.setText(mySelectSonarResourcesFrom.getText() + " " + mySonarServerName);
    init();
  }

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
    List<SonarResource> sonarResources = SonarResourcesService.getInstance().sonarResourcesBySonarServerName.get(mySonarServerName);
    if (null == sonarResources) sonarResources = new ArrayList<SonarResource>();
    myResourcesTable.setModelAndUpdateColumns(new ListTableModel<SonarResource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN}, sonarResources, 0));
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

  private JComponent createResourcesTableComponent() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myResourcesTable, null).
        disableUpDownActions().
        disableAddAction().disableRemoveAction().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 400));
    return panelForTable;
  }

  private class DownloadResourcesRunnable implements Runnable {

    @Override
    public void run() {
      final Optional<SonarServerConfigurationBean> sonarServerConfiguration = SonarServersService.get(mySonarServerName);
      if (sonarServerConfiguration.isPresent()) {
        final org.sonar.wsclient.Sonar sonar = SonarServer.getInstance().createSonar(sonarServerConfiguration.get());
        try {
          allProjectsAndModules = SonarServer.getInstance().getAllProjectsAndModules(sonar);
          final List<SonarResource> sonarResources = new ArrayList<SonarResource>(allProjectsAndModules.size());
          for (Resource resource : allProjectsAndModules) {
            final SonarResource sonarResource = new SonarResource(resource);
            sonarResources.add(sonarResource);
          }
          SonarResourcesService.getInstance().sonarResourcesBySonarServerName.put(mySonarServerName, ImmutableList.copyOf(sonarResources));
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              myResourcesTable.setModelAndUpdateColumns(new ListTableModel<SonarResource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN}, sonarResources, 0));
            }
          });
        } catch (Exception e) {
          final String message = "Cannot fetch sonar project and modules from " + mySonarServerName
              + "\n\n" + Throwables.getStackTraceAsString(e);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              Messages.showErrorDialog(message, "Sonar Server Error");
            }
          });
        }
      }
    }
  }

  @Override
  protected void doOKAction() {
    final int[] selectedRowsIndex = myResourcesTable.getSelectedRows();
    ListTableModel<SonarResource> sonarResoures = (ListTableModel<SonarResource>) myResourcesTable.getModel();
    selectedSonarResources = new ArrayList<Resource>(selectedRowsIndex.length);
    for (int i: selectedRowsIndex) {
      Resource sonarResource = allProjectsAndModules.get(i);
      selectedSonarResources.add(sonarResource);
    }
    super.doOKAction();
  }
}
