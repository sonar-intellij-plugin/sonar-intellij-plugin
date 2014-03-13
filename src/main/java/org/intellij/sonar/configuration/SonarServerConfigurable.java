package org.intellij.sonar.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SonarServerConfigurable extends DialogWrapper {
  private JCheckBox myAnonymousCheckBox;
  private JCheckBox myShowPasswordCheckBox;
  private JTextField myNameTestField;
  private JTextField myHostUrlTextField;
  private JTextField myUserTextField;
  private JPasswordField myPasswordField;
  private JPanel myRootPanel;
  private Project myProject;

  public SonarServerConfigurable(@Nullable Project project) {
    super(project);
    init();
    myProject = project;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {

    setEnabledForCredentialsTextFields();
    setPasswordVisibility();

    myAnonymousCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        setEnabledForCredentialsTextFields();
      }
    });
    myShowPasswordCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        setPasswordVisibility();
      }
    });

    return myRootPanel;
  }

  private void setPasswordVisibility() {
    if (myShowPasswordCheckBox.isSelected()) {
      makePasswordVisible();
    } else {
      makePasswordInvisible();
    }
  }

  private void setEnabledForCredentialsTextFields() {
    final boolean isAnonymousCheckBoxSelected = myAnonymousCheckBox.isSelected();
    myUserTextField.setEnabled(!isAnonymousCheckBoxSelected);
    myPasswordField.setEnabled(!isAnonymousCheckBoxSelected);
  }

  private void makePasswordInvisible() {
    myPasswordField.setEchoChar('•');
  }

  private void makePasswordVisible() {
    myPasswordField.setEchoChar('\u0000');
  }
}
