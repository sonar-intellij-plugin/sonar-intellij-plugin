package org.intellij.sonar.configuration.module;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import org.intellij.sonar.configuration.IncrementalScriptsMapping;
import org.intellij.sonar.configuration.SonarResourceMapping;
import org.intellij.sonar.configuration.SonarServerConfigurable;
import org.intellij.sonar.persistence.ModuleSettingsBean;
import org.intellij.sonar.persistence.ModuleSettingsComponent;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;


public class ModuleSettingsConfigurable implements Configurable, ModuleComponent {

  private static final Logger LOG = Logger.getInstance(ModuleSettingsConfigurable.class);
  private static final String NO_SONAR = "<NO SONAR>";
  private static final String PROJECT_SONAR = "<PROJECT>";
  private final TableView<SonarResourceMapping> mySonarResourcesTable;
  private final TableView<IncrementalScriptsMapping> myIncrementalAnalysisScriptsTable;
  private Module myModule;
  private JButton myTestConfigurationButton;
  private JPanel myRootJPanel;
  private JPanel myPanelForSonarResources;
  private JPanel myPanelForIncrementalAnalysisScripts;
  private JComboBox mySonarServersComboBox;
  private JButton myAddSonarServerButton;
  private JButton myEditSonarServerButton;
  private JButton myRemoveSonarServerButton;

  public ModuleSettingsConfigurable(Module module) {
    this.myModule = module;
    this.mySonarResourcesTable = new TableView<SonarResourceMapping>();
    this.myIncrementalAnalysisScriptsTable = new TableView<IncrementalScriptsMapping>();
  }

  private JComponent createSonarResourcesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(mySonarResourcesTable, null).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  private JComponent createIncrementalAnalysisScriptsTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myIncrementalAnalysisScriptsTable, null).
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
    disableEditAndRemoveButtonsIfNoSonarOrProjectSelected(mySonarServersComboBox);
    return myRootJPanel;
  }

  private void initSonarServersCombobox() {
    Optional<Collection<SonarServerConfigurationBean>> sonarServerConfigurationBeans = SonarServersService.getAll();
    if (sonarServerConfigurationBeans.isPresent()) {
      mySonarServersComboBox.removeAllItems();
      mySonarServersComboBox.addItem(makeObj(PROJECT_SONAR));
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
        disableEditAndRemoveButtonsIfNoSonarOrProjectSelected(sonarServersComboBox);
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
          disableEditAndRemoveButtonsIfNoSonarOrProjectSelected(mySonarServersComboBox);
        }
      }
    });
  }

  private void disableEditAndRemoveButtonsIfNoSonarOrProjectSelected(JComboBox sonarServersComboBox) {
    final boolean isNoSonarSelected = NO_SONAR.equals(sonarServersComboBox.getSelectedItem().toString());
    final boolean isProjectSonarSelected = PROJECT_SONAR.equals(sonarServersComboBox.getSelectedItem().toString());
    myEditSonarServerButton.setEnabled(!isNoSonarSelected && !isProjectSonarSelected);
    myRemoveSonarServerButton.setEnabled(!isNoSonarSelected && !isProjectSonarSelected);
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
    final SonarServerConfigurable dlg = new SonarServerConfigurable(myModule.getProject());
    if (null != oldSonarServerConfigurationBean) dlg.setValuesFrom(oldSonarServerConfigurationBean);
    dlg.show();
    return dlg;
  }

  @Override
  public boolean isModified() {
//    ModuleSettingsBean state = myModule.getComponent(ModuleSettingsComponent.class).getState();
//    return null == state || !state.equals(this.toProjectSettingsBean());
    return false;
  }

  @Override
  public void apply() throws ConfigurationException {
//    ModuleSettingsBean projectSettingsBean = this.toProjectSettingsBean();
//    ModuleSettingsComponent projectSettingsComponent = myModule.getComponent(ModuleSettingsComponent.class);
//    projectSettingsComponent.loadState(projectSettingsBean);
//    PasswordManager.storePassword(myModule.getMyProject(), projectSettingsBean);
  }

  @Override
  public void reset() {
//    ModuleSettingsComponent projectSettingsComponent = myModule.getComponent(ModuleSettingsComponent.class);
//    if (projectSettingsComponent != null && projectSettingsComponent.getState() != null) {
//      ModuleSettingsBean persistedState = projectSettingsComponent.getState();
//      this.setValuesFromProjectSettingsBean(persistedState);
//    }
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
  public void moduleAdded() {

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

  public ModuleSettingsBean toProjectSettingsBean() {

    ModuleSettingsBean projectSettingsBean = new ModuleSettingsBean();
//    projectSettingsBean.sonarServerHostUrl = this.getSonarServerTextField().getText();
//    projectSettingsBean.useAnonymous = this.getUseAnonymousCheckBox().isSelected();
//    projectSettingsBean.user = this.getUserTextField().getText();
//    projectSettingsBean.password = String.valueOf(this.getPasswordField().getPassword());

    ModuleSettingsBean persistedProjectSettingsBean = myModule.getComponent(ModuleSettingsComponent.class).getState();
    if (persistedProjectSettingsBean != null) {
//      projectSettingsBean.downloadedResources = persistedProjectSettingsBean.downloadedResources;
    }
    convertResourcesListToBean(projectSettingsBean);

    return projectSettingsBean;
  }

  private void convertResourcesListToBean(ModuleSettingsBean projectSettingsBean) {
//    ListModel model = this.getResourcesList().getModel();
//    projectSettingsBean.resources = new ArrayList<String>(model.getSize());
//    for (int i = 0; i < model.getSize(); i++) {
//      projectSettingsBean.resources.add((String) model.getElementAt(i));
//    }
  }

  public void setValuesFromProjectSettingsBean(ModuleSettingsBean projectSettingsBean) {
    if (null == projectSettingsBean) return;

//    this.getSonarServerTextField().setText(projectSettingsBean.sonarServerHostUrl);
//    this.getUseAnonymousCheckBox().setSelected(projectSettingsBean.useAnonymous);
//    this.getUserTextField().setText(projectSettingsBean.user);
//    this.getPasswordField().setText(projectSettingsBean.password);
  }

}
