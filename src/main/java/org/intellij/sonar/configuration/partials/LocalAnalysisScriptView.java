package org.intellij.sonar.configuration.partials;

import com.google.common.base.Throwables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.LocalAnalysisScriptConfigurable;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.intellij.sonar.util.UIUtil;

import javax.swing.*;
import java.util.Optional;

import static org.intellij.sonar.util.UIUtil.makeObj;

public abstract class LocalAnalysisScriptView {

  protected final Project myProject;
  protected final JComboBox myLocalAnalysisScriptComboBox;
  protected final JButton myAddLocalAnalysisScriptButton;
  protected final JButton myEditLocalAnalysisScriptButton;
  protected final JButton myRemoveLocalAnalysisScriptButton;

  public LocalAnalysisScriptView(
    JComboBox localAnalysisScriptComboBox,
    JButton addLocalAnalysisScriptButton,
    JButton editLocalAnalysisScriptButton,
    JButton removeLocalAnalysisScriptButton,
    Project project
  ) {
    this.myProject = project;
    this.myLocalAnalysisScriptComboBox = localAnalysisScriptComboBox;
    this.myAddLocalAnalysisScriptButton = addLocalAnalysisScriptButton;
    this.myEditLocalAnalysisScriptButton = editLocalAnalysisScriptButton;
    this.myRemoveLocalAnalysisScriptButton = removeLocalAnalysisScriptButton;
  }

  public void init() {
    addActionListenersForButtons();
    initComboBox();
    disableEditAndRemoveButtonsIfPossible();
  }

  private void addActionListenersForButtons() {
    addItemListenersForLocalAnalysisScriptComboBox();
    addActionListenersForAddLocalAnalysisScriptButton();
    addActionListenersForEditLocalAnalysisScriptButton();
    addActionListenersForRemoveLocalAnalysisScriptButton();
  }

  private void addItemListenersForLocalAnalysisScriptComboBox() {
    myLocalAnalysisScriptComboBox.addItemListener(
        itemEvent -> disableEditAndRemoveButtonsIfPossible()
    );
  }

  private void addActionListenersForAddLocalAnalysisScriptButton() {
    myAddLocalAnalysisScriptButton.addActionListener(
        e -> {
          final LocalAnalysisScriptConfigurable dlg = showLocalAnalysisScriptConfigurableDialog();
          if (dlg.isOK()) {
            final LocalAnalysisScript newLocalAnalysisScript = dlg.toLocalAnalysisScript();
            try {
              LocalAnalysisScripts.add(newLocalAnalysisScript);
              myLocalAnalysisScriptComboBox.addItem(makeObj(newLocalAnalysisScript.getName()));
              UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox,newLocalAnalysisScript.getName());
            } catch (IllegalArgumentException ex) {
              final String errorMessage = newLocalAnalysisScript.getName()+" already exists";
              SonarConsole.get(myProject).error(errorMessage+"\n"+ Throwables.getStackTraceAsString(ex));
              Messages.showErrorDialog(errorMessage,"Local Analysis Script Name Error");
              showLocalAnalysisScriptConfigurableDialog(newLocalAnalysisScript);
            }
          }
        }
    );
  }

  private void addActionListenersForEditLocalAnalysisScriptButton() {
    myEditLocalAnalysisScriptButton.addActionListener(
        actionEvent -> {
          final Object selected = myLocalAnalysisScriptComboBox.getSelectedItem();
          final Optional<LocalAnalysisScript> previous = LocalAnalysisScripts.get(selected.toString());
          if (!previous.isPresent()) {
            Messages.showErrorDialog(selected.toString()+" is not more preset","Cannot Perform Edit");
          } else {
            final LocalAnalysisScriptConfigurable dlg = showLocalAnalysisScriptConfigurableDialog(previous.get());
            if (dlg.isOK()) {
              performEdit(selected, previous.get(), dlg);
            }
          }
        }
    );
  }

  private void performEdit(Object selected, LocalAnalysisScript previous, LocalAnalysisScriptConfigurable dlg) {
    LocalAnalysisScript next = dlg.toLocalAnalysisScript();
    try {
      LocalAnalysisScripts.remove(previous.getName());
      LocalAnalysisScripts.add(next);
      myLocalAnalysisScriptComboBox.removeItem(selected);
      myLocalAnalysisScriptComboBox.addItem(makeObj(next.getName()));
      UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox,next.getName());
    } catch (IllegalArgumentException e) {
      Messages.showErrorDialog(
        selected.toString()+" cannot be saved\n\n"+ Throwables.getStackTraceAsString(e),
        "Cannot Perform Edit"
      );
    }
  }

  private void addActionListenersForRemoveLocalAnalysisScriptButton() {
    myRemoveLocalAnalysisScriptButton.addActionListener(
        actionEvent -> {
          final Object selected = myLocalAnalysisScriptComboBox.getSelectedItem();
          int rc = Messages.showOkCancelDialog(
            "Are you sure you want to remove "+selected.toString()+" ?",
            "Remove Local Analysis Script",
            "Yes, remove", "No",
            Messages.getQuestionIcon()
          );
          if (rc == Messages.OK) {
            LocalAnalysisScripts.remove(selected.toString());
            myLocalAnalysisScriptComboBox.removeItem(selected);
            disableEditAndRemoveButtonsIfPossible();
          }
        }
    );
  }

  protected abstract void initComboBox();
  protected abstract boolean editAndRemoveButtonsCanBeEnabled();

  protected void disableEditAndRemoveButtonsIfPossible() {
    final boolean enabled = editAndRemoveButtonsCanBeEnabled();
    myEditLocalAnalysisScriptButton.setEnabled(enabled);
    myRemoveLocalAnalysisScriptButton.setEnabled(enabled);
  }

  protected LocalAnalysisScriptConfigurable showLocalAnalysisScriptConfigurableDialog() {
    return showLocalAnalysisScriptConfigurableDialog(null);
  }

  protected LocalAnalysisScriptConfigurable showLocalAnalysisScriptConfigurableDialog(LocalAnalysisScript
    localAnalysisScript) {
    final LocalAnalysisScriptConfigurable dlg = new LocalAnalysisScriptConfigurable(myProject);
    if (null != localAnalysisScript)
      dlg.setValuesFrom(localAnalysisScript);
    dlg.show();
    return dlg;
  }
}
