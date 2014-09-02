package org.intellij.sonar.configuration;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.application.ApplicationManager;
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
import org.intellij.sonar.persistence.SonarResourcesComponent;
import org.intellij.sonar.persistence.SonarServerConfiguration;
import org.intellij.sonar.persistence.SonarServers;
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

  private static final ColumnInfo<Resource, String> NAME_COLUMN = new ColumnInfo<Resource, String>("Name") {
    @Nullable
    @Override
    public String valueOf(Resource resource) {
      return SonarResource.of(resource).getName();
    }

    @Override
    public int getWidth(JTable table) {
      return 300;
    }
  };
  private static final ColumnInfo<Resource, String> KEY_COLUMN = new ColumnInfo<Resource, String>("Key") {
    @Nullable
    @Override
    public String valueOf(Resource resource) {
      return SonarResource.of(resource).getKey();
    }
  };
  private Project myProject;
  private String mySonarServerName;
  private TableView<Resource> myResourcesTable = new TableView<Resource>();
  private JButton myDownloadResourcesButton;
  private JPanel myRootJPanel;
  private JLabel mySelectSonarResourcesFrom;
  private JPanel myPanelForSonarResources;
  private List<Resource> myAllProjectsAndModules;

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

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanelForSonarResources.setLayout(new BorderLayout());
    myPanelForSonarResources.add(createResourcesTableComponent(), BorderLayout.CENTER);
    myAllProjectsAndModules = SonarResourcesComponent.getInstance().sonarResourcesBySonarServerName.get(mySonarServerName);
    if (null == myAllProjectsAndModules) myAllProjectsAndModules = new ArrayList<Resource>();
    myResourcesTable.setModelAndUpdateColumns(new ListTableModel<Resource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN}, myAllProjectsAndModules, 0));
    new TableSpeedSearch(myResourcesTable);

    myDownloadResourcesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        DownloadResourcesRunnable downloadResourcesRunnable = new DownloadResourcesRunnable();
        ProgressManager.getInstance().runProcessWithProgressSynchronously(downloadResourcesRunnable, "Loading sonar resources from server", true, myProject);
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
      final Optional<SonarServerConfiguration> sonarServerConfiguration = SonarServers.get(mySonarServerName);
      if (sonarServerConfiguration.isPresent()) {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfiguration.get());
        try {
          myAllProjectsAndModules = sonarServer.getAllProjectsAndModules();
          SonarResourcesComponent.getInstance().sonarResourcesBySonarServerName.put(mySonarServerName, ImmutableList.copyOf(myAllProjectsAndModules));
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              myResourcesTable.setModelAndUpdateColumns(new ListTableModel<Resource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN}, myAllProjectsAndModules, 0));
            }
          });
        } catch (Exception e) {
          final String message = "Cannot fetch sonar project and modules from " + mySonarServerName
              + "\n\n" + Throwables.getStackTraceAsString(e);
          ApplicationManager.getApplication().invokeLater(new Runnable() {
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
    selectedSonarResources = new ArrayList<Resource>(selectedRowsIndex.length);
    for (int i: selectedRowsIndex) {
      Resource sonarResource = myAllProjectsAndModules.get(i);
      selectedSonarResources.add(sonarResource);
    }
    super.doOKAction();
  }
}
