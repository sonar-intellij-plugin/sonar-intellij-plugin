package org.intellij.sonar.configuration.partials;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.SonarServerConfigurable;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.intellij.sonar.persistence.SonarServersComponent;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class SonarServersView {

  protected final JComboBox mySonarServersComboBox;
  protected final JButton myAddSonarServerButton;
  protected final JButton myEditSonarServerButton;
  protected final JButton myRemoveSonarServerButton;
  protected final Project myProject;

  public SonarServersView(JComboBox mySonarServersComboBox, JButton myAddSonarServerButton, JButton myEditSonarServerButton, JButton myRemoveSonarServerButton, Project myProject) {
    this.mySonarServersComboBox = mySonarServersComboBox;
    this.myAddSonarServerButton = myAddSonarServerButton;
    this.myEditSonarServerButton = myEditSonarServerButton;
    this.myRemoveSonarServerButton = myRemoveSonarServerButton;
    this.myProject = myProject;
  }

  public void init() {
    addActionListenersForSonarServerButtons();
    initSonarServersComboBox();
    disableEditAndRemoveButtonsIfPossible();
  }

  public String getSelectedItemFromComboBox() {
    return mySonarServersComboBox.getSelectedItem().toString();
  }

  public String getSelectedItem() {
    return getSelectedItemFromComboBox();
  }

  abstract boolean editAndRemoveButtonsCanBeEnabled();

  protected void disableEditAndRemoveButtonsIfPossible() {
    final boolean enabled = editAndRemoveButtonsCanBeEnabled();
    myEditSonarServerButton.setEnabled(enabled);
    myRemoveSonarServerButton.setEnabled(enabled);
  }

  public void selectItemForSonarServersComboBoxByName(String name) {
    Optional itemToSelect = Optional.absent();
    for (int i = 0; i < mySonarServersComboBox.getItemCount(); i++) {
      final Object item = mySonarServersComboBox.getItemAt(i);
      if (name.equals(item.toString())) {
        itemToSelect = Optional.of(item);
      }
    }
    if (itemToSelect.isPresent()) mySonarServersComboBox.setSelectedItem(itemToSelect.get());
  }

  protected SonarServerConfigurable showSonarServerConfigurableDialog() {
    return showSonarServerConfigurableDialog(null);
  }

  protected SonarServerConfigurable showSonarServerConfigurableDialog(SonarServerConfigurationBean oldSonarServerConfigurationBean) {
    final SonarServerConfigurable dlg = new SonarServerConfigurable(myProject);
    if (null != oldSonarServerConfigurationBean) dlg.setValuesFrom(oldSonarServerConfigurationBean);
    dlg.show();
    return dlg;
  }

  protected Object makeObj(final String item) {
    return new Object() {
      public String toString() {
        return item;
      }
    };
  }

  abstract void initSonarServersComboBox();

  protected void addActionListenersForSonarServerButtons() {

    final JComboBox sonarServersComboBox = mySonarServersComboBox;

    sonarServersComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        disableEditAndRemoveButtonsIfPossible();
      }
    });

    myAddSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {

        final SonarServerConfigurable dlg = showSonarServerConfigurableDialog();
        if (dlg.isOK()) {
          SonarServerConfigurationBean newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
          try {
            SonarServersComponent.add(newSonarConfigurationBean);
            mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.getName()));
            selectItemForSonarServersComboBoxByName(newSonarConfigurationBean.getName());
          } catch (IllegalArgumentException e) {
            Messages.showErrorDialog(newSonarConfigurationBean.getName() + " already exists", "Sonar Name Error");
            showSonarServerConfigurableDialog(newSonarConfigurationBean);
          }
        }
      }
    });

    myEditSonarServerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final Object selectedSonarServer = sonarServersComboBox.getSelectedItem();
        final Optional<SonarServerConfigurationBean> oldBean = SonarServersComponent.get(selectedSonarServer.toString());
        if (!oldBean.isPresent()) {
          Messages.showErrorDialog(selectedSonarServer.toString() + " is not more preset", "Cannot Perform Edit");
        } else {
          final SonarServerConfigurable dlg = showSonarServerConfigurableDialog(oldBean.get());
          if (dlg.isOK()) {
            SonarServerConfigurationBean newSonarConfigurationBean = dlg.toSonarServerConfigurationBean();
            try {
              SonarServersComponent.remove(oldBean.get().getName());
              SonarServersComponent.add(newSonarConfigurationBean);
              mySonarServersComboBox.removeItem(selectedSonarServer);
              mySonarServersComboBox.addItem(makeObj(newSonarConfigurationBean.getName()));
              selectItemForSonarServersComboBoxByName(newSonarConfigurationBean.getName());
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
          SonarServersComponent.remove(selectedSonarServer.toString());
          mySonarServersComboBox.removeItem(selectedSonarServer);
          disableEditAndRemoveButtonsIfPossible();
        }
      }
    });
  }
}
