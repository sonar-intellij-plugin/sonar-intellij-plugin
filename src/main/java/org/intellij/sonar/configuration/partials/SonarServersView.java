package org.intellij.sonar.configuration.partials;

import static org.intellij.sonar.util.UIUtil.makeObj;

import java.util.Optional;

import javax.swing.*;

import com.google.common.base.Throwables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.SonarServerConfigurable;
import org.intellij.sonar.console.ConsoleLogLevel;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.util.UIUtil;

public abstract class SonarServersView {

  protected final JComboBox mySonarServersComboBox;
  protected final JButton myAddSonarServerButton;
  protected final JButton myEditSonarServerButton;
  protected final JButton myRemoveSonarServerButton;
  protected final Project myProject;

  public SonarServersView(
    JComboBox mySonarServersComboBox,
    JButton myAddSonarServerButton,
    JButton myEditSonarServerButton,
    JButton myRemoveSonarServerButton,
    Project myProject
  ) {
    this.mySonarServersComboBox = mySonarServersComboBox;
    this.myAddSonarServerButton = myAddSonarServerButton;
    this.myEditSonarServerButton = myEditSonarServerButton;
    this.myRemoveSonarServerButton = myRemoveSonarServerButton;
    this.myProject = myProject;
  }

  public void init() {
    addActionListenersForButtons();
    initSonarServersComboBox();
    disableEditAndRemoveButtonsIfPossible();
  }

  public String getSelectedItemFromComboBox() {
    return mySonarServersComboBox.getSelectedItem().toString();
  }

  public String getSelectedItem() {
    return getSelectedItemFromComboBox();
  }

  protected abstract boolean editAndRemoveButtonsCanBeEnabled();
  protected abstract void initSonarServersComboBox();

  protected void disableEditAndRemoveButtonsIfPossible() {
    final boolean enabled = editAndRemoveButtonsCanBeEnabled();
    myEditSonarServerButton.setEnabled(enabled);
    myRemoveSonarServerButton.setEnabled(enabled);
  }

  protected SonarServerConfigurable showSonarServerConfigurableDialog() {
    return showSonarServerConfigurableDialog(null);
  }

  protected SonarServerConfigurable showSonarServerConfigurableDialog(SonarServerConfig oldSonarServerConfigBean) {
    final SonarServerConfigurable dlg = new SonarServerConfigurable(myProject);
    if (null != oldSonarServerConfigBean)
      dlg.setValuesFrom(oldSonarServerConfigBean);
    dlg.show();
    return dlg;
  }

  protected void addActionListenersForButtons() {
    addItemListenerForSonarServersComboBox();
    addActionListenerForAddSonarServerButton();
    addActionListenerForEditSonarServerButton();
    addActionListenerForRemoveSonarServerButton();
  }

  private void addItemListenerForSonarServersComboBox() {
    mySonarServersComboBox.addItemListener(
        itemEvent -> disableEditAndRemoveButtonsIfPossible()
    );
  }

  private void addActionListenerForAddSonarServerButton() {
    myAddSonarServerButton.addActionListener(
        actionEvent -> {
          final SonarServerConfigurable dlg = showSonarServerConfigurableDialog();
          if (dlg.isOK()) {
            SonarServerConfig newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
            try {
              SonarServers.add(newSonarConfigurationBean);
              mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.getName()));
              UIUtil.selectComboBoxItem(mySonarServersComboBox,newSonarConfigurationBean.getName());
            } catch (IllegalArgumentException e) {
              Messages.showErrorDialog(newSonarConfigurationBean.getName()+" already exists","SonarQube Name Error");
              showSonarServerConfigurableDialog(newSonarConfigurationBean);
              SonarConsole.get(myProject).log(Throwables.getStackTraceAsString(e), ConsoleLogLevel.ERROR);
            }
          }
        }
    );
  }

  private void addActionListenerForEditSonarServerButton() {
    myEditSonarServerButton.addActionListener(
        actionEvent -> {
          final Object selectedSonarServer = mySonarServersComboBox.getSelectedItem();
          final Optional<SonarServerConfig> oldBean = SonarServers.get(selectedSonarServer.toString());
          if (!oldBean.isPresent()) {
            Messages.showErrorDialog(selectedSonarServer.toString()+" is not more preset","Cannot Perform Edit");
          } else {
            final SonarServerConfigurable dlg = showSonarServerConfigurableDialog(oldBean.get());
            if (dlg.isOK()) {
              performEdit(selectedSonarServer, oldBean.get(), dlg);
            }
          }
        }
    );
  }

  private void performEdit(Object selectedSonarServer, SonarServerConfig oldBean, SonarServerConfigurable dlg) {
    SonarServerConfig newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
    try {
      SonarServers.remove(oldBean.getName());
      SonarServers.add(newSonarConfigurationBean);
      mySonarServersComboBox.removeItem(selectedSonarServer);
      mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.getName()));
      UIUtil.selectComboBoxItem(mySonarServersComboBox,newSonarConfigurationBean.getName());
    } catch (IllegalArgumentException e) {
      Messages.showErrorDialog(
        selectedSonarServer.toString()+" cannot be saved\n\n"+ Throwables.getStackTraceAsString(
          e
        ),"Cannot Perform Edit"
      );
    }
  }

  private void addActionListenerForRemoveSonarServerButton() {
    myRemoveSonarServerButton.addActionListener(
        actionEvent -> {
          final Object selectedSonarServer = mySonarServersComboBox.getSelectedItem();
          int rc = Messages.showOkCancelDialog(
            "Are you sure you want to remove "+selectedSonarServer.toString()+" ?",
            "Remove SonarQube Server",
            "Yes, remove", "No",
            Messages.getQuestionIcon()
          );
          if (rc == Messages.OK) {
            SonarServers.remove(selectedSonarServer.toString());
            mySonarServersComboBox.removeItem(selectedSonarServer);
            disableEditAndRemoveButtonsIfPossible();
          }
        }
    );
  }

  public JComboBox getSonarServersComboBox() {
    return mySonarServersComboBox;
  }

  public JButton getAddSonarServerButton() {
    return myAddSonarServerButton;
  }

  public JButton getEditSonarServerButton() {
    return myEditSonarServerButton;
  }

  public JButton getRemoveSonarServerButton() {
    return myRemoveSonarServerButton;
  }
}
