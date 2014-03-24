package org.intellij.sonar.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SourcePathConfigurable extends DialogWrapper {
  private JPanel myRootPanel;
  private TextFieldWithBrowseButton mySourcePath;

  protected SourcePathConfigurable(@Nullable Project project) {
    super(project);
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return myRootPanel;
  }
}
