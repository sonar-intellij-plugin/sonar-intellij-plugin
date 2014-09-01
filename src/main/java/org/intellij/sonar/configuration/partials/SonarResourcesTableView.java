package org.intellij.sonar.configuration.partials;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.intellij.sonar.configuration.ResourcesSelectionConfigurable;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.intellij.sonar.persistence.SonarServers.NO_SONAR;

public class SonarResourcesTableView {

  private final TableView<Resource> mySonarResourcesTable;
  private final JComponent myJComponent;
  private final SonarServersView mySonarServersView;
  private final Project myProject;


  public SonarResourcesTableView(Project project, SonarServersView sonarServersView) {
    this.mySonarServersView = sonarServersView;
    this.mySonarResourcesTable = new TableView<Resource>();
    this.myJComponent = createJComponent();
    this.myProject = project;
  }

  private static final ColumnInfo<Resource, String> TYPE_COLUMN = new ColumnInfo<Resource, String>("Type") {
    @Nullable
    @Override
    public String valueOf(Resource sonarResource) {
      if (Resource.QUALIFIER_PROJECT.equals(sonarResource.getQualifier())) {
        return "Project";
      } else if (Resource.QUALIFIER_MODULE.equals(sonarResource.getQualifier())) {
        return "Module";
      } else {
        return sonarResource.getQualifier();
      }
    }

  };

  private static final ColumnInfo<Resource, String> NAME_COLUMN = new ColumnInfo<Resource, String>("Name") {
    @Nullable
    @Override
    public String valueOf(Resource sonarResource) {
      return sonarResource.getName();
    }

    @Override
    public int getWidth(JTable table) {
      return 300;
    }

  };

  private static final ColumnInfo<Resource, String> KEY_COLUMN = new ColumnInfo<Resource, String>("Key") {

    @Nullable
    @Override
    public String valueOf(Resource sonarResource) {
      return sonarResource.getKey();
    }

  };

  private JComponent createJComponent() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(mySonarResourcesTable, null).
        setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            final String selectedSonarServerName = mySonarServersView.getSelectedItem();
            if (!NO_SONAR.equals(selectedSonarServerName)) {
              ResourcesSelectionConfigurable dlg = new ResourcesSelectionConfigurable(myProject, selectedSonarServerName);
              dlg.show();
              if (dlg.isOK()) {
                final java.util.List<Resource> selectedSonarResources = dlg.getSelectedSonarResources();
                final java.util.List<Resource> currentSonarResources = mySonarResourcesTable.getItems();

                Set<Resource> mergedSonarResourcesAsSet = new TreeSet<Resource>(new Comparator<Resource>() {
                  @Override
                  public int compare(Resource resource, Resource resource2) {
                    return resource.getKey().compareTo(resource2.getKey());
                  }
                });
                mergedSonarResourcesAsSet.addAll(currentSonarResources);
                mergedSonarResourcesAsSet.addAll(selectedSonarResources);

                setModel(Lists.newArrayList(mergedSonarResourcesAsSet));
              }
            }
          }
        }).
        setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            TableUtil.removeSelectedItems(mySonarResourcesTable);
          }
        })
        .disableUpDownActions().
            createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 100));
    return panelForTable;
  }

  public void setModel(List<Resource> sonarResources) {
    mySonarResourcesTable.setModelAndUpdateColumns(new ListTableModel<Resource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN, TYPE_COLUMN}, sonarResources, 0));
  }

  public TableView<Resource> getTable() {
    return mySonarResourcesTable;
  }

  public JComponent getComponent() {
    return myJComponent;
  }
}
