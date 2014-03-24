package org.intellij.sonar.configuration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.Consumer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IncrementalScriptConfigurable extends DialogWrapper {

  public static final ColumnInfo<VirtualFile, String> SOURCE_PATH_COLUMN = new ColumnInfo<VirtualFile, String>("Source path") {
    @Nullable
    @Override
    public String valueOf(VirtualFile virtualFile) {
      return virtualFile.getPath();
    }
  };
  private Project myProject;
  private JPanel myRootPanel;
  private JTextPane myScriptSourceTextPane;
  private JPanel myPanelForSourcePaths;
  private TextFieldWithBrowseButton myPathToSonarReportTextFieldWithBrowseButton;

  private final TableView<VirtualFile> mySourcePathsTable;

  public IncrementalScriptConfigurable(@Nullable Project project) {
    super(project);
    myProject = project;
    mySourcePathsTable = new TableView<VirtualFile>();
    init();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanelForSourcePaths.setLayout(new BorderLayout());
    myPanelForSourcePaths.add(createScriptSourcePathesTable(), BorderLayout.CENTER);

    myPathToSonarReportTextFieldWithBrowseButton.addActionListener(new ExternalEditorPathActionListener());

    return myRootPanel;
  }

  private JComponent createScriptSourcePathesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(mySourcePathsTable, null).
        setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            final VirtualFile projectBaseDir = myProject.getBaseDir();

            FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(false, true, false, false, false, true);
            fileDescriptor.setShowFileSystemRoots(true);
            fileDescriptor.setTitle("Configure Path");
            fileDescriptor.setDescription("Configure sonar source path for incremental analysis script");
            FileChooser.chooseFiles(fileDescriptor, myProject, projectBaseDir, new Consumer<java.util.List<VirtualFile>>() {
              @Override
              public void consume(final java.util.List<VirtualFile> files) {
                mySourcePathsTable.setModelAndUpdateColumns(new ListTableModel<VirtualFile>(new ColumnInfo[]{SOURCE_PATH_COLUMN}, files, 0));
              }
            });
          }
        }).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 200));
    return panelForTable;
  }

  private final class ExternalEditorPathActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      Application application = ApplicationManager.getApplication();
      VirtualFile previous = application.runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final String path = FileUtil.toSystemIndependentName(myPathToSonarReportTextFieldWithBrowseButton.getText());
          return LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        }
      });
      FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(true, SystemInfo.isMac, false, false, false, false);
      fileDescriptor.setShowFileSystemRoots(true);
      fileDescriptor.setTitle("Configure Path");
      fileDescriptor.setDescription("Configure path to sonar-report.json");
      FileChooser.chooseFiles(fileDescriptor, null, previous, new Consumer<java.util.List<VirtualFile>>() {
        @Override
        public void consume(final java.util.List<VirtualFile> files) {
          String path = files.get(0).getPath();
          myPathToSonarReportTextFieldWithBrowseButton.setText(path);
        }
      });
    }
  }

}
