package org.intellij.sonar.configuration.project;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.intellij.sonar.configuration.IncrementalScriptsMapping;
import org.intellij.sonar.configuration.ResourcesSelectionConfigurable;
import org.intellij.sonar.configuration.SonarServerConfigurable;
import org.intellij.sonar.persistence.ProjectSettingsBean;
import org.intellij.sonar.persistence.ProjectSettingsComponent;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;


public class ProjectSettingsConfigurable implements Configurable, ProjectComponent {

  public ProjectSettingsConfigurable(Project project) {
    this.myProject = project;
    this.mySonarResourcesTable = new TableView<Resource>();
    this.myIncrementalAnalysisScriptsTable = new TableView<IncrementalScriptsMapping>();
    this.myProjectSettingsComponent = myProject.getComponent(ProjectSettingsComponent.class);
  }

  private static final Logger LOG = Logger.getInstance(ProjectSettingsConfigurable.class);
  private static final String NO_SONAR = "<NO SONAR>";

  private final TableView<Resource> mySonarResourcesTable;
  private final TableView<IncrementalScriptsMapping> myIncrementalAnalysisScriptsTable;
  private Project myProject;
  private JButton myTestConfigurationButton;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JPanel myPanelForIncrementalAnalysisScripts;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;
  private final ProjectSettingsComponent myProjectSettingsComponent;

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

  private JComponent createSonarResourcesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(mySonarResourcesTable, null).
        setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            final String selectedSonarServerName = mySonarServersComboBox.getSelectedItem().toString();
            if (!NO_SONAR.equals(selectedSonarServerName)) {
              ResourcesSelectionConfigurable dlg = new ResourcesSelectionConfigurable(myProject, selectedSonarServerName);
              dlg.show();
              if (dlg.isOK()) {
                final java.util.List<Resource> selectedSonarResources = dlg.getSelectedSonarResources();
                final java.util.List<Resource> currentSonarResources = getCurrentSonarResources();
                final java.util.List<Resource> mergedSonarResources = Lists.newArrayList(ImmutableSet.copyOf(Iterables.concat(currentSonarResources, selectedSonarResources)).asList());
                setModelForSonarResourcesTable(mergedSonarResources);
              }
            }
          }
        }).
        setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            TableUtil.removeSelectedItems(mySonarResourcesTable);
          }
        }).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  private void setModelForSonarResourcesTable(List<Resource> sonarResources) {
    mySonarResourcesTable.setModelAndUpdateColumns(new ListTableModel<Resource>(new ColumnInfo[]{NAME_COLUMN, KEY_COLUMN, TYPE_COLUMN}, sonarResources, 0));
  }

  private java.util.List<Resource> getCurrentSonarResources() {
    return mySonarResourcesTable.getListTableModel().getItems();
  }

  private JComponent createIncrementalAnalysisScriptsTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myIncrementalAnalysisScriptsTable, null).
        setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {

          }
        }).
        setEditActionName("Edit").
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return "SonarQube";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  public JButton getMyTestConfigurationButton() {
    return myTestConfigurationButton;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    myPanelForSonarResources.setLayout(new BorderLayout());
    myPanelForSonarResources.add(createSonarResourcesTable(), BorderLayout.CENTER);
    myPanelForIncrementalAnalysisScripts.setLayout(new BorderLayout());
    myPanelForIncrementalAnalysisScripts.add(createIncrementalAnalysisScriptsTable(), BorderLayout.CENTER);

    addActionListenersForSonarServerButtons();
    initSonarServersCombobox();
    disableEditAndRemoveButtonsIfNoSonarSelected(mySonarServersComboBox);
    return myRootJPanel;
  }

  private void initSonarServersCombobox() {
    Optional<Collection<SonarServerConfigurationBean>> sonarServerConfigurationBeans = SonarServersService.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(NO_SONAR));
      for (SonarServerConfigurationBean sonarServerConfigurationBean : sonarServerConfigurationBeans.get()) {
        mySonarServersComboBox.addItem(makeObj(sonarServerConfigurationBean.name));
      }
    }
  }

  private Object makeObj(final String item) {
    return new Object() {
      public String toString() {
        return item;
      }
    };
  }

  private void addActionListenersForSonarServerButtons() {

    final JComboBox sonarServersComboBox = mySonarServersComboBox;

    sonarServersComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        disableEditAndRemoveButtonsIfNoSonarSelected(sonarServersComboBox);
      }
    });

    myAddSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

        final SonarServerConfigurable dlg = showSonarServerConfigurableDialog();
        if (dlg.isOK()) {
          SonarServerConfigurationBean newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
          try {
            SonarServersService.add(newSonarConfigurationBean);
            mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.name));
            selectItemForSonarServersComboBoxByName(newSonarConfigurationBean.name);
          } catch (IllegalArgumentException e) {
            Messages.showErrorDialog(newSonarConfigurationBean.name + " already exists", "Sonar Name Error");
            showSonarServerConfigurableDialog(newSonarConfigurationBean);
          }
        }
      }
    });

    myEditSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final Object selectedSonarServer = sonarServersComboBox.getSelectedItem();
        final Optional<SonarServerConfigurationBean> oldBean = SonarServersService.get(selectedSonarServer.toString());
        if (!oldBean.isPresent()) {
          Messages.showErrorDialog(selectedSonarServer.toString() + " is not more preset", "Cannot Perform Edit");
        } else {
          final SonarServerConfigurable dlg = showSonarServerConfigurableDialog(oldBean.get());
          if (dlg.isOK()) {
            SonarServerConfigurationBean newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
            try {
              SonarServersService.remove(oldBean.get().name);
              SonarServersService.add(newSonarConfigurationBean);
              mySonarServersComboBox.removeItem(selectedSonarServer);
              mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.name));
              selectItemForSonarServersComboBoxByName(newSonarConfigurationBean.name);
            } catch (IllegalArgumentException e) {
              Messages.showErrorDialog(selectedSonarServer.toString() + " cannot be saved\n\n" + Throwables.getStackTraceAsString(e), "Cannot Perform Edit");
            }
          }
        }
      }
    });

    myRemoveSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final Object selectedSonarServer = sonarServersComboBox.getSelectedItem();
        int rc = Messages.showOkCancelDialog("Are you sure you want to remove " + selectedSonarServer.toString() + " ?", "Remove Sonar Server", Messages.getQuestionIcon());
        if (rc == Messages.OK) {
          SonarServersService.remove(selectedSonarServer.toString());
          mySonarServersComboBox.removeItem(selectedSonarServer);
          disableEditAndRemoveButtonsIfNoSonarSelected(mySonarServersComboBox);
        }
      }
    });
  }

  private void disableEditAndRemoveButtonsIfNoSonarSelected(JComboBox sonarServersComboBox) {
    final boolean isNoSonarSelected = NO_SONAR.equals(sonarServersComboBox.getSelectedItem().toString());
    myEditSonarServerButton.setEnabled(!isNoSonarSelected);
    myRemoveSonarServerButton.setEnabled(!isNoSonarSelected);
  }

  private void selectItemForSonarServersComboBoxByName(String name) {
    Optional itemToSelect = Optional.absent();
    for (int i = 0; i < mySonarServersComboBox.getItemCount(); i++) {
      final Object item = mySonarServersComboBox.getItemAt(i);
      if (name.equals(item.toString())) {
        itemToSelect = Optional.of(item);
      }
    }
    if (itemToSelect.isPresent()) mySonarServersComboBox.setSelectedItem(itemToSelect.get());
  }

  private SonarServerConfigurable showSonarServerConfigurableDialog() {
    return showSonarServerConfigurableDialog(null);
  }

  private SonarServerConfigurable showSonarServerConfigurableDialog(SonarServerConfigurationBean oldSonarServerConfigurationBean) {
    final SonarServerConfigurable dlg = new SonarServerConfigurable(myProject);
    if (null != oldSonarServerConfigurationBean) dlg.setValuesFrom(oldSonarServerConfigurationBean);
    dlg.show();
    return dlg;
  }

  @Override
  public boolean isModified() {
    if (null == myProjectSettingsComponent) return false;
    ProjectSettingsBean state = myProjectSettingsComponent.getState();
    return null == state || !state.equals(this.toProjectSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    myProjectSettingsComponent.loadState(this.toProjectSettingsBean());
  }

  @Override
  public void reset() {
    if (myProjectSettingsComponent != null && myProjectSettingsComponent.getState() != null) {
      ProjectSettingsBean persistedState = myProjectSettingsComponent.getState();
      this.setValuesFromProjectSettingsBean(persistedState);
    }
  }

  @Override
  public void disposeUIResources() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void projectOpened() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void projectClosed() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initComponent() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void disposeComponent() {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "SonarQube";
  }

  public ProjectSettingsBean toProjectSettingsBean() {

    ProjectSettingsBean projectSettingsBean = new ProjectSettingsBean();
    projectSettingsBean.sonarServerName = mySonarServersComboBox.getSelectedItem().toString();
    projectSettingsBean.resources = ImmutableList.copyOf(getCurrentSonarResources());

    ProjectSettingsBean persistedProjectSettingsBean = myProjectSettingsComponent.getState();
    if (persistedProjectSettingsBean != null) {
//      projectSettingsBean.downloadedResources = persistedProjectSettingsBean.downloadedResources;
    }
    convertResourcesListToBean(projectSettingsBean);

    return projectSettingsBean;
  }

  private void convertResourcesListToBean(ProjectSettingsBean projectSettingsBean) {
//    ListModel model = this.getResourcesList().getModel();
//    projectSettingsBean.resources = new ArrayList<String>(model.getSize());
//    for (int i = 0; i < model.getSize(); i++) {
//      projectSettingsBean.resources.add((String) model.getElementAt(i));
//    }
  }

  public void setValuesFromProjectSettingsBean(ProjectSettingsBean projectSettingsBean) {

    if (null == projectSettingsBean) return;
    selectItemForSonarServersComboBoxByName(projectSettingsBean.sonarServerName);

    final ArrayList<Resource> resources = Lists.newArrayList(projectSettingsBean.resources);
    setModelForSonarResourcesTable(resources);

  }

}
