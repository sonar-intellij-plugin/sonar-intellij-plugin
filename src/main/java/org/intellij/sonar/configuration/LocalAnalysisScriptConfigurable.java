package org.intellij.sonar.configuration;

import com.google.common.base.Strings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class LocalAnalysisScriptConfigurable extends DialogWrapper {

  private static final MouseAdapter SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      Messages.showMessageDialog(
              AVAILABLE_TEMPLATE_VARIABLES_MESSAGE,
        "Analysis Script",AllIcons.Actions.Help
      );
    }
  };

  private static final String AVAILABLE_TEMPLATE_VARIABLES_MESSAGE = "Available template variables:\n" +
          "\n" +
          "$WORKING_DIR\n" +
          "$WORKING_DIR_NAME\n" +
          "\n" +
          "$MODULE_NAME\n" +
          "$MODULE_BASE_DIR\n" +
          "$MODULE_BASE_DIR_NAME\n" +
          "\n" +
          "$PROJECT_NAME\n" +
          "$PROJECT_BASE_DIR\n" +
          "$PROJECT_BASE_DIR_NAME\n" +
          "\n" +
          "$SONAR_HOST_URL\n" +
          "$SONAR_SERVER_NAME\n" +
          "$SONAR_USER_NAME\n" +
          "$SONAR_USER_PASSWORD\n";

  private Project myProject;
  private JPanel myRootPanel;
  private JLabel labelForSourceCodeOfIncrementalAnalysisScript;
  private JTextPane myScriptSourceTextPane;
  private JLabel labelForPathToSonarReport;
  private TextFieldWithBrowseButton myPathToSonarReportTextFieldWithBrowseButton;
  private JTextField myNameTextField;

  public LocalAnalysisScriptConfigurable(@Nullable Project project) {
    super(project);
    myProject = project;
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    labelForSourceCodeOfIncrementalAnalysisScript.setIcon(AllIcons.Actions.Help);
    labelForSourceCodeOfIncrementalAnalysisScript.setHorizontalTextPosition(JLabel.LEFT);
    labelForSourceCodeOfIncrementalAnalysisScript.addMouseListener(
      SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT
    );
    labelForPathToSonarReport.setIcon(AllIcons.Actions.Help);
    labelForPathToSonarReport.setHorizontalTextPosition(JLabel.LEFT);
    labelForPathToSonarReport.addMouseListener(SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT);
    myPathToSonarReportTextFieldWithBrowseButton.addActionListener(new ExternalEditorPathActionListener());
    return myRootPanel;
  }

  public LocalAnalysisScript toLocalAnalysisScript() {
    return LocalAnalysisScript.of(
      myNameTextField.getText(),
      StringUtil.trimLeading(myScriptSourceTextPane.getText()),
      myPathToSonarReportTextFieldWithBrowseButton.getText().trim()
    );
  }

  public void setValuesFrom(LocalAnalysisScript s) {
    myNameTextField.setText(s.getName());
    myScriptSourceTextPane.setText(StringUtil.trimLeading(Strings.nullToEmpty(s.getSourceCode())));
    myPathToSonarReportTextFieldWithBrowseButton.setText(Strings.nullToEmpty(s.getPathToSonarReport()).trim());
  }

  private final class ExternalEditorPathActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      Application application = ApplicationManager.getApplication();
      VirtualFile previous = application.runWriteAction(
              (NullableComputable<VirtualFile>) () -> {
                final String path = FileUtil.toSystemIndependentName(myPathToSonarReportTextFieldWithBrowseButton.getText
                    ());
                return !StringUtil.isEmptyOrSpaces(path)
                  ? LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
                  : null;
              }
      );
      FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(true,false,false,false,false,false);
      fileDescriptor.setShowFileSystemRoots(true);
      fileDescriptor.setTitle("Configure Path");
      fileDescriptor.setDescription("Configure path to sonar-report.json");
      FileChooser.chooseFiles(
        fileDescriptor,
        myProject,
          Optional.ofNullable(previous).orElse(ProjectUtil.guessProjectDir(myProject)),
          files -> {
            String path = files.get(0).getPath();
            myPathToSonarReportTextFieldWithBrowseButton.setText(path);
          }
      );
    }
  }
}
