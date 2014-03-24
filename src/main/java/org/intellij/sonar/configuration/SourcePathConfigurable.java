package org.intellij.sonar.configuration;

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
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
      VirtualFile previous = application.runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final String path = FileUtil.toSystemIndependentName(mySourcePathTextFieldWithBrowseButton.getText());
          return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        }
      });
      FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false, true, false, false, false, true);
      fileDescriptor.setShowFileSystemRoots(true);
      fileDescriptor.setTitle("Configure Path");
      fileDescriptor.setDescription("Configure sonar source path for incremental analysis script");
      FileChooser.chooseFiles(fileDescriptor, null, previous, new Consumer<List<VirtualFile>>() {
        @Override
        public void consume(final java.util.List<VirtualFile> files) {
          String path = files.get(0).getPath();
          mySourcePathTextFieldWithBrowseButton.setText(path);
        }
      });
    }
  }
}
