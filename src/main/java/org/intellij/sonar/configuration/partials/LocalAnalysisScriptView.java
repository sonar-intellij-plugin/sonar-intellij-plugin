package org.intellij.sonar.configuration.partials;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.intellij.sonar.configuration.LocalAnalysisScriptConfigurable;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.intellij.sonar.util.UIUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.intellij.sonar.util.UIUtil.makeObj;

public abstract class LocalAnalysisScriptView {

  protected final Project myProject;
  protected final JComboBox myLocalAnalysisScriptComboBox;
  protected final JButton myAddLocalAnalysisScriptButton;
  protected final JButton myEditLocalAnalysisScriptButton;
  protected final JButton myRemoveLocalAnalysisScriptButton;

  public LocalAnalysisScriptView(JComboBox localAnalysisScriptComboBox, JButton addLocalAnalysisScriptButton, JButton editLocalAnalysisScriptButton, JButton removeLocalAnalysisScriptButton, Project project) {
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

    myLocalAnalysisScriptComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent itemEvent) {
        disableEditAndRemoveButtonsIfPossible();
      }
    });

    myAddLocalAnalysisScriptButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final LocalAnalysisScriptConfigurable dlg = showLocalAnalysisScriptConfigurableDialog();
        if (dlg.isOK()) {
          final LocalAnalysisScript newLocalAnalysisScript = dlg.toLocalAnalysisScript();
          try {
            LocalAnalysisScripts.add(newLocalAnalysisScript);
            myLocalAnalysisScriptComboBox.addItem(makeObj(newLocalAnalysisScript.getName()));
            UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox, newLocalAnalysisScript.getName());
          } catch (IllegalArgumentException ex) {
            Messages.showErrorDialog(newLocalAnalysisScript.getName() + " already exists", "Local Analysis Script Name Error");
            showLocalAnalysisScriptConfigurableDialog(newLocalAnalysisScript);
          }
        }
      }
    });

    myEditLocalAnalysisScriptButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final Object selected = myLocalAnalysisScriptComboBox.getSelectedItem();
        final Optional<LocalAnalysisScript> previous = LocalAnalysisScripts.get(selected.toString());
        if (!previous.isPresent()) {
          Messages.showErrorDialog(selected.toString() + " is not more preset", "Cannot Perform Edit");
        } else {
          final LocalAnalysisScriptConfigurable dlg = showLocalAnalysisScriptConfigurableDialog(previous.get());
          if (dlg.isOK()) {
            LocalAnalysisScript next = dlg.toLocalAnalysisScript();
            try {
              LocalAnalysisScripts.remove(previous.get().getName());
              LocalAnalysisScripts.add(next);
              myLocalAnalysisScriptComboBox.removeItem(selected);
              myLocalAnalysisScriptComboBox.addItem(makeObj(next.getName()));
              UIUtil.selectComboBoxItem(myLocalAnalysisScriptComboBox, next.getName());
            } catch (IllegalArgumentException e) {
              Messages.showErrorDialog(selected.toString() + " cannot be saved\n\n" + Throwables.getStackTraceAsString(e), "Cannot Perform Edit");
            }
          }
        }
      }
    });

    myRemoveLocalAnalysisScriptButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        final Object selected = myLocalAnalysisScriptComboBox.getSelectedItem();
        int rc = Messages.showOkCancelDialog("Are you sure you want to remove " + selected.toString() + " ?", "Remove Local Analysis Script", Messages.getQuestionIcon());
        if (rc == Messages.OK) {
          LocalAnalysisScripts.remove(selected.toString());
          myLocalAnalysisScriptComboBox.removeItem(selected);
          disableEditAndRemoveButtonsIfPossible();
        }
      }
    });
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

  protected LocalAnalysisScriptConfigurable showLocalAnalysisScriptConfigurableDialog(LocalAnalysisScript localAnalysisScript) {
    final LocalAnalysisScriptConfigurable dlg = new LocalAnalysisScriptConfigurable(myProject);
    if (null != localAnalysisScript) dlg.setValuesFrom(localAnalysisScript);
    dlg.show();
    return dlg;
  }
}
