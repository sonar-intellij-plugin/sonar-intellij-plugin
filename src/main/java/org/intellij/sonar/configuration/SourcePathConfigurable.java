package org.intellij.sonar.configuration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public class SourcePathConfigurable extends DialogWrapper {

  private JPanel myRootPanel;
  private TextFieldWithBrowseButton mySourcePathTextFieldWithBrowseButton;

  protected SourcePathConfigurable(@Nullable Project project) {
    super(project);
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    mySourcePathTextFieldWithBrowseButton.addActionListener(new ExternalEditorPathActionListener());
    return myRootPanel;
  }

  private final class ExternalEditorPathActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      Application application = ApplicationManager.getApplication();
      VirtualFile previous = application.runWriteAction(
              (NullableComputable<VirtualFile>) () -> {
                final String path = FileUtil.toSystemIndependentName(mySourcePathTextFieldWithBrowseButton.getText());
                return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
              }
      );
      FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false,true,false,false,false,true);
      fileDescriptor.setShowFileSystemRoots(true);
      fileDescriptor.setTitle("Configure Path");
      fileDescriptor.setDescription("Configure SonarQube source path for incremental analysis script");
      FileChooser.chooseFiles(
        fileDescriptor,null,previous, files -> {
          String path = files.get(0).getPath();
          mySourcePathTextFieldWithBrowseButton.setText(path);
        }
      );
    }
  }
}
