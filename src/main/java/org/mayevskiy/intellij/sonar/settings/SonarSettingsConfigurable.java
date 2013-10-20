package org.mayevskiy.intellij.sonar.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.sonarserver.SonarServerConnectionException;
import org.mayevskiy.intellij.sonar.sonarserver.SonarService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.Collection;

/**
 * @author Oleg Mayevskiy
 * @author Michail Plushnikov
 */
public abstract class SonarSettingsConfigurable implements Configurable {
  private static final String DEFAULT_SERVER_URL = "";
  private static final String DEFAULT_USER = "";
  private static final String DEFAULT_PASSWORD = "";
  private static final String DEFAULT_RESOURCE = "";

  private String sonarServerVersion;
  private String sonarServerError;

  private ResourcesComboBoxItemListener resourcesComboBoxItemListener;

  public String getSonarServerVersion() {
    return sonarServerVersion;
  }

  public void setSonarServerVersion(String sonarServerVersion) {
    this.sonarServerVersion = sonarServerVersion;
  }

  public String getSonarServerError() {
    return sonarServerError;
  }

  public void setSonarServerError(String sonarServerError) {
    this.sonarServerError = sonarServerError;
  }

  public abstract JButton getTestConnectionButton();

  public abstract JButton getUpdateResourcesButton();

  public abstract SonarService getSonarService();

  public abstract SonarSettingsComponent getSonarSettingsComponent();

  public abstract Project getProject();

  public abstract JPanel getJPanel();

  public abstract JTextField getSonarServerUrlTextField();

  public abstract JTextField getSonarUserTextField();

  public abstract JTextField getSonarPasswordTextField();

  public abstract JTextField getSonarResourceTextField();

  public abstract JComboBox<String> getSonarResourceComboBox();

  @Nls
  @Override
  public String getDisplayName() {
    return "Sonar Connector";
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return null;
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    fromSettingsBean(getSonarSettingsComponent().getState());

    getTestConnectionButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

        boolean processed = ProgressManager.getInstance().runProcessWithProgressSynchronously(
            new TestConnectionRunnable(),
            "Testing Connection", true, getProject()
        );

        if (processed) {
          Messages.showMessageDialog(MessageFormat.format("Connection successful\nSonar v.{0}", getSonarServerVersion()), "Connection Test", Messages.getInformationIcon());
        } else {
          Messages.showMessageDialog(MessageFormat.format("Connection not successful\n\n{0}", getSonarServerError()), "Connection Test", Messages.getErrorIcon());
        }
      }
    });

    getUpdateResourcesButton().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        fillSonarResourceCombobox(getSonarService(), toSettingsBean());
      }
    });

    resourcesComboBoxItemListener = new ResourcesComboBoxItemListener();

    return getJPanel();
  }

  private void fromSettingsBean(SonarSettingsBean state) {
    if (null == state) {
      getSonarServerUrlTextField().setText(DEFAULT_SERVER_URL);
      getSonarUserTextField().setText(DEFAULT_USER);
      getSonarPasswordTextField().setText(DEFAULT_PASSWORD);
      getSonarResourceTextField().setText(DEFAULT_RESOURCE);
    } else {
      getSonarServerUrlTextField().setText(state.host);
      getSonarUserTextField().setText(state.user);
      getSonarPasswordTextField().setText(state.password);
      getSonarResourceTextField().setText(state.resource);
    }
  }

  private SonarSettingsBean toSettingsBean() {
    SonarSettingsBean result = new SonarSettingsBean();
    result.host = getSonarServerUrlTextField().getText();
    result.user = getSonarUserTextField().getText();
    result.password = getSonarPasswordTextField().getText();
    result.resource = getSonarResourceTextField().getText();
    return result;
  }

  @Override
  public boolean isModified() {
    SonarSettingsBean state = getSonarSettingsComponent().getState();
    return null == state || !state.equals(toSettingsBean());
  }

  @Override
  public void apply() throws ConfigurationException {
    getSonarSettingsComponent().loadState(toSettingsBean());
  }

  @Override
  public void reset() {
    fromSettingsBean(getSonarSettingsComponent().getState());
  }

  @Override
  public void disposeUIResources() {
  }

  private class TestConnectionRunnable implements Runnable {

    @Override
    public void run() {
      ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      indicator.setText("Testing Connection");
      indicator.setText2(String.format("connecting to %s", getSonarServerUrlTextField().getText()));
      indicator.setFraction(0.5);
      indicator.setIndeterminate(true);

      try {
        setSonarServerVersion(getSonarService().verifySonarConnection(toSettingsBean()));
      } catch (SonarServerConnectionException ex) {
        setSonarServerError(ex.getMessage());
        throw new ProcessCanceledException();
      } catch (Exception re) {
        throw new ProcessCanceledException();
      }

      if (indicator.isCanceled()) {
        throw new ProcessCanceledException();
      }
    }
  }

  private void fillSonarResourceCombobox(SonarService sonarService, SonarSettingsBean sonarSettingsBean) {
    Collection<String> sonarProjects = sonarService.getProjects(sonarSettingsBean);
    JComboBox<String> sonarResourceComboBox = getSonarResourceComboBox();
    sonarResourceComboBox.removeItemListener(resourcesComboBoxItemListener);
    sonarResourceComboBox.removeAllItems();
    for (String sonarProjectKey : sonarProjects) {
      sonarResourceComboBox.addItem(sonarProjectKey);
    }
    sonarResourceComboBox.addItemListener(resourcesComboBoxItemListener);
  }

  private class ResourcesComboBoxItemListener implements ItemListener {
    @Override
    public void itemStateChanged(ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        getSonarResourceTextField().setText((String) e.getItem());
      }
    }
  }
}
