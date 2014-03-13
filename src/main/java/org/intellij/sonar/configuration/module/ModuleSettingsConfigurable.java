package org.intellij.sonar.configuration.module;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import org.intellij.sonar.configuration.IncrementalScriptsMapping;
import org.intellij.sonar.configuration.SonarResourceMapping;
import org.intellij.sonar.persistence.ModuleSettingsBean;
import org.intellij.sonar.persistence.ModuleSettingsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;


public class ModuleSettingsConfigurable implements Configurable, ModuleComponent {

  private static final Logger LOG = Logger.getInstance(ModuleSettingsConfigurable.class);
  private final TableView<SonarResourceMapping> sonarResourcesTable;
  private final TableView<IncrementalScriptsMapping> incrementalAnalysisScriptsTable;
  private Module module;
  private JButton testConfigurationButton;
  private JPanel rootJPanel;
  private JPanel panelForSonarResources;
  private JPanel panelForIncrementalAnalysisScripts;
  private JComboBox sonarServersComboBox;
  private JButton addButton;
  private JButton editButton;
  private JButton removeButton;

  public ModuleSettingsConfigurable(Module module) {
    this.module = module;
    this.sonarResourcesTable = new TableView<SonarResourceMapping>();
    this.incrementalAnalysisScriptsTable = new TableView<IncrementalScriptsMapping>();
  }

  public Module getModule() {
    return module;
  }

  public JPanel getRootJPanel() {
    return rootJPanel;
  }

  private JComponent createSonarResourcesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(sonarResourcesTable, null).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  private JComponent createIncrementalAnalysisScriptsTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(incrementalAnalysisScriptsTable, null).
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

  public JButton getTestConfigurationButton() {
    return testConfigurationButton;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    ModuleSettingsComponent projectSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
    if (null != projectSettingsComponent) {
      ModuleSettingsBean projectSettingsBean = projectSettingsComponent.getState();
      this.setValuesFromProjectSettingsBean(projectSettingsBean);
//      setCredentialsEnabled();
    }

//    getUseAnonymousCheckBox().addChangeListener(new ChangeListener() {
//      @Override
//      public void stateChanged(ChangeEvent changeEvent) {
//        setCredentialsEnabled();
//      }
//    });

//    getShowPasswordCheckBox().addChangeListener(new ChangeListener() {
//      @Override
//      public void stateChanged(ChangeEvent changeEvent) {
//        if (getShowPasswordCheckBox().isSelected()) {
//          makePasswordVisible();
//        } else {
//          makePasswordInvisible();
//        }
//      }
//    });

//    getTestConfigurationButton().addActionListener(new ActionListener() {
//      @Override
//      public void actionPerformed(ActionEvent actionEvent) {
//        String foo = ";";
//      }
//    });

    panelForSonarResources.setLayout(new BorderLayout());
    panelForSonarResources.add(createSonarResourcesTable(), BorderLayout.CENTER);
    panelForIncrementalAnalysisScripts.setLayout(new BorderLayout());
    panelForIncrementalAnalysisScripts.add(createIncrementalAnalysisScriptsTable(), BorderLayout.CENTER);

    return getRootJPanel();
  }

//  private void setCredentialsEnabled() {
//    getUserTextField().setEnabled(!getUseAnonymousCheckBox().isSelected());
//    getPasswordField().setEnabled(!getUseAnonymousCheckBox().isSelected());
//  }

  @Override
  public boolean isModified() {
    ModuleSettingsBean state = module.getComponent(ModuleSettingsComponent.class).getState();
    return null == state || !state.equals(this.toProjectSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    ModuleSettingsBean projectSettingsBean = this.toProjectSettingsBean();
    ModuleSettingsComponent projectSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
    projectSettingsComponent.loadState(projectSettingsBean);
//    PasswordManager.storePassword(module.getMyProject(), projectSettingsBean);
  }

  @Override
  public void reset() {
    ModuleSettingsComponent projectSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
    if (projectSettingsComponent != null && projectSettingsComponent.getState() != null) {
      ModuleSettingsBean persistedState = projectSettingsComponent.getState();
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

    ModuleSettingsBean persistedProjectSettingsBean = module.getComponent(ModuleSettingsComponent.class).getState();
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
