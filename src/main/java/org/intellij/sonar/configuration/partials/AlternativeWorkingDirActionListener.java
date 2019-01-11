package org.intellij.sonar.configuration.partials;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

public final class AlternativeWorkingDirActionListener implements ActionListener {

  private final Project project;
  private final TextFieldWithBrowseButton textFieldWithBrowseButton;
  private final VirtualFile dirToSelect;

  public AlternativeWorkingDirActionListener(
    Project project,
    TextFieldWithBrowseButton textFieldWithBrowseButton,
    VirtualFile dirToSelect
  ) {
    this.project = project;
    this.textFieldWithBrowseButton = textFieldWithBrowseButton;
    this.dirToSelect = dirToSelect;
  }

  public void actionPerformed(ActionEvent e) {
    Application application = ApplicationManager.getApplication();
    FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false,true,false,false,false,false);
    fileDescriptor.setShowFileSystemRoots(true);
    fileDescriptor.setTitle("Configure Path");
    fileDescriptor.setDescription("Configure working directory");
    FileChooser.chooseFiles(
      fileDescriptor,
      project,
        Optional.ofNullable(previousVirtualFile(application)).orElse(dirToSelect),
            files -> {
              String path = files.get(0).getPath();
              textFieldWithBrowseButton.setText(path);
            }
    );
  }

  private VirtualFile previousVirtualFile(Application application) {
    return application.runWriteAction(
            (NullableComputable<VirtualFile>) () -> {
              final String path = FileUtil.toSystemIndependentName(textFieldWithBrowseButton.getText());
              return
                !StringUtil.isEmptyOrSpaces(path)
                  ?
                  LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
                  : null;
            }
    );
  }
}
