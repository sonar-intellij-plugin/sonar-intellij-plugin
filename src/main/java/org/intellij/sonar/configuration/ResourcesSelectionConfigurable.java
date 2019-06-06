package org.intellij.sonar.configuration;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.*;

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
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.SonarResource;
import org.intellij.sonar.persistence.SonarResourcesComponent;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourcesSelectionConfigurable extends DialogWrapper {

  private static final ColumnInfo<Resource,String> NAME_COLUMN = new ColumnInfo<Resource,String>("Name") {
    @Nullable
    @Override
    public String valueOf(Resource resource) {
      return SonarResource.of(resource).getName();
    }
  };
  private static final ColumnInfo<Resource,String> KEY_COLUMN = new ColumnInfo<Resource,String>("Key") {
    @Nullable
    @Override
    public String valueOf(Resource resource) {
      return SonarResource.of(resource).getKey();
    }
  };
  private Project myProject;
  private String mySonarServerName;
  private TableView<Resource> myResourcesTable = new TableView<>();
  private JButton myDownloadResourcesButton;
  private JPanel myRootJPanel;
  private JLabel mySelectSonarResourcesFrom;
  private JPanel myPanelForSonarResources;
  private JTextField myProjectNameFilterTextField;
  private List<Resource> myAllProjectsAndModules;
  private List<Resource> selectedSonarResources;

  public ResourcesSelectionConfigurable(@Nullable Project project,@NotNull String sonarServerName) {
    super(project);
    myProject = project;
    mySonarServerName = sonarServerName;
    mySelectSonarResourcesFrom.setText(mySelectSonarResourcesFrom.getText()+" "+mySonarServerName);
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanelForSonarResources.setLayout(new BorderLayout());
    myPanelForSonarResources.add(createResourcesTableComponent(),BorderLayout.CENTER);
    myAllProjectsAndModules = SonarResourcesComponent.getInstance().sonarResourcesBySonarServerName.get(
      mySonarServerName
    );
    if (null == myAllProjectsAndModules)
      myAllProjectsAndModules = new ArrayList<>();
    myResourcesTable.setModelAndUpdateColumns(
      new ListTableModel<>(
        new ColumnInfo[]{NAME_COLUMN,KEY_COLUMN},
        myAllProjectsAndModules,
        0
      )
    );
    new TableSpeedSearch(myResourcesTable);
    myDownloadResourcesButton.addActionListener(myDownloadResourcesButtonActionListener());
    return myRootJPanel;
  }

  @NotNull
  private ActionListener myDownloadResourcesButtonActionListener() {
    return actionEvent -> {
      DownloadResourcesRunnable downloadResourcesRunnable = new DownloadResourcesRunnable(
              myProjectNameFilterTextField.getText()
      );
      ProgressManager.getInstance()
        .runProcessWithProgressSynchronously(
          downloadResourcesRunnable,
          "Loading SonarQube resources from server",
          true,
          myProject
        );
    };
  }

  private JComponent createResourcesTableComponent() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myResourcesTable,null).
      disableUpDownActions().
      disableAddAction().disableRemoveAction().
      createPanel();
    panelForTable.setPreferredSize(new Dimension(-1,400));
    return panelForTable;
  }

  private class DownloadResourcesRunnable implements Runnable {

    private final String projectNameFilter;


    public DownloadResourcesRunnable(String projectNameFilter) {
      this.projectNameFilter = projectNameFilter;
    }

    @Override
    public void run() {
      final Optional<SonarServerConfig> sonarServerConfiguration = SonarServers.get(mySonarServerName);
      if (sonarServerConfiguration.isPresent()) {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfiguration.get());
        try {
          myAllProjectsAndModules = sonarServer.getAllProjectsAndModules(projectNameFilter);
          SonarResourcesComponent.getInstance().sonarResourcesBySonarServerName.put(
            mySonarServerName,
            ImmutableList.copyOf(myAllProjectsAndModules)
          );
          ApplicationManager.getApplication().invokeLater(
              () -> myResourcesTable.setModelAndUpdateColumns(
                new ListTableModel<>(
                  new ColumnInfo[]{
                    NAME_COLUMN,
                    KEY_COLUMN
                  },myAllProjectsAndModules,0
                )
              )
          );
        } catch (Exception e) {
          final String message = "Cannot fetch SonarQube project and modules from "+mySonarServerName
            +"\n\n"+Throwables.getStackTraceAsString(e);
          ApplicationManager.getApplication().invokeLater(
              () -> Messages.showErrorDialog(message,"SonarQube Server Error")
          );
        }
      }
    }

  }
  @Override
  protected void doOKAction() {
    final int[] selectedRowsIndex = myResourcesTable.getSelectedRows();
    selectedSonarResources = new ArrayList<>(selectedRowsIndex.length);
    for (int i : selectedRowsIndex) {
      Resource sonarResource = myAllProjectsAndModules.get(i);
      selectedSonarResources.add(sonarResource);
    }
    super.doOKAction();
  }

  public List<Resource> getSelectedSonarResources() {
    return selectedSonarResources;
  }
}
