package org.intellij.sonar.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import org.intellij.sonar.persistence.SonarServerConfigurationBean;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

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

  @Override
  protected void doOKAction() {
    if (StringUtil.isEmptyOrSpaces(myNameTestField.getText())) {
      Messages.showErrorDialog("Please provide an unique name for the sonar server", "Empty Name");
    } else if (!isHostUrlSyntaxCorrect()) {
      Messages.showErrorDialog("Host url syntax should be: http(s)://your.host(:1234)/possible/path", "Malformed Host Url");
    } else if (!myAnonymousCheckBox.isSelected() && StringUtil.isEmptyOrSpaces(myUserTextField.getText())) {
      Messages.showErrorDialog("User may not be empty", "Empty User");
    } else {
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

  public SonarServerConfigurationBean toSonarServerConfigurationBean() {
    SonarServerConfigurationBean bean = new SonarServerConfigurationBean();
    bean.name = myNameTestField.getText();
    bean.hostUrl = myHostUrlTextField.getText();
    bean.user = myUserTextField.getText();
    bean.password = String.valueOf(myPasswordField.getPassword());
    bean.anonymous = myAnonymousCheckBox.isSelected();
    return bean;
  }

  public void setValuesFrom(SonarServerConfigurationBean bean) {
    this.myNameTestField.setText(bean.name);
    this.myHostUrlTextField.setText(bean.hostUrl);
    if (!bean.anonymous) {
      this.myUserTextField.setText(bean.user);
      bean.loadPassword();
      this.myPasswordField.setText(bean.password);
      bean.password = null;
    }
    this.myAnonymousCheckBox.setSelected(bean.anonymous);

    initCheckboxes();
  }
}
