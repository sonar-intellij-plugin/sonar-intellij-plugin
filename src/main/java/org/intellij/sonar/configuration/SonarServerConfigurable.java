package org.intellij.sonar.configuration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.jetbrains.annotations.Nullable;

public class SonarServerConfigurable extends DialogWrapper {

  private JCheckBox myAnonymousCheckBox;
  private JCheckBox myShowPasswordCheckBox;
  private JTextField myNameTestField;
  private JTextField myHostUrlTextField;
  private JTextField myUserTextField;
  private JPasswordField myPasswordField;
  private boolean isPasswordFieldChanged = false;
  private JPanel myRootPanel;

  public SonarServerConfigurable(@Nullable Project project) {
    super(project);
    myPasswordField.addKeyListener(
      new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent keyEvent) {
          super.keyTyped(keyEvent);
          isPasswordFieldChanged = true;
        }
      }
    );
    init();
  }

  @Override
  protected void doOKAction() {
    if (StringUtil.isEmptyOrSpaces(myNameTestField.getText())) {
      Messages.showErrorDialog("Please provide an unique name for the SonarQube server","Empty Name");
    } else
      if (!isHostUrlSyntaxCorrect()) {
        Messages.showErrorDialog(
          "Host url syntax should be: http(s)://your.host(:1234)/possible/path",
          "Malformed Host Url"
        );
      } else
        if (!myAnonymousCheckBox.isSelected() && StringUtil.isEmptyOrSpaces(myUserTextField.getText())) {
          Messages.showErrorDialog("User may not be empty","Empty User");
        } else {
          if (myAnonymousCheckBox.isSelected()) {
            myUserTextField.setText("");
            myPasswordField.setText("");
          }
          super.doOKAction();
        }
  }

  private boolean isHostUrlSyntaxCorrect() {
    try {
      new URL(myHostUrlTextField.getText());
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    initCheckboxes();
    myAnonymousCheckBox.addActionListener(
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
          setEnabledForCredentialsTextFields();
        }
      }
    );
    myShowPasswordCheckBox.addActionListener(
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
          setPasswordVisibility();
        }
      }
    );
    return myRootPanel;
  }

  private void initCheckboxes() {
    setEnabledForCredentialsTextFields();
    setPasswordVisibility();
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

  public SonarServerConfig toSonarServerConfigurationBean() {
    SonarServerConfig sonarServerConfig = SonarServerConfig.of(
      myNameTestField.getText(),
      myHostUrlTextField.getText(),
      myAnonymousCheckBox.isSelected(),
      myUserTextField.getText()
    );
    sonarServerConfig.setPassword(String.valueOf(myPasswordField.getPassword()));
    sonarServerConfig.setPasswordChanged(isPasswordFieldChanged);
    return sonarServerConfig;
  }

  public void setValuesFrom(SonarServerConfig bean) {
    this.myNameTestField.setText(bean.getName());
    this.myHostUrlTextField.setText(bean.getHostUrl());
    if (!bean.isAnonymous()) {
      this.myUserTextField.setText(bean.getUser());
      this.myPasswordField.setText(bean.loadPassword());
      bean.clearPassword();
    }
    this.myAnonymousCheckBox.setSelected(bean.isAnonymous());
    initCheckboxes();
  }
}
