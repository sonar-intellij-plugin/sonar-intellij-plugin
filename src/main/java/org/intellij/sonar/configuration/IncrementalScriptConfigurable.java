package org.intellij.sonar.configuration;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.Consumer;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class IncrementalScriptConfigurable extends DialogWrapper {

  public static final ColumnInfo<VirtualFile, String> SOURCE_PATH_COLUMN = new ColumnInfo<VirtualFile, String>("Source path") {
    @Nullable
    @Override
    public String valueOf(VirtualFile virtualFile) {
      return virtualFile != null ? virtualFile.getPath() : "";
    }
  };

  public static final MouseAdapter SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      Messages.showMessageDialog("Basically a sonar incremental analysis is a call of sonar-runner in incremental mode.\n" +
              "A script using maven sonar plugin could look like:\n\n" +
              "mvn sonar:sonar -DskipTests=true -Dsonar.analysis.mode=incremental -Dsonar.host.url=http://sonar\n\n" +
              "Another script using plain sonar-runner could look like:\n\n" +
              "sonar-runner -Dsonar.analysis.mode=incremental -Dsonar.host.url=http://sonar\n\n" +
              "If you prefer use of ant, gradle or yet another build tool you like, it's up to you to setup it properly.\n\n" +
              "Doesn't matter which tool you use, all of them must generate a sonar-report.json and you need to setup the location of it as well.\n" +
              "Example location for a maven build: target/sonar/sonar-report.json\n\n" +
              "Note: if of some reason the incremental mode does not work for you, then you could switch to preview mode\n" +
              "by changing -Dsonar.analysis.mode=incremental to -Dsonar.analysis.mode=preview .\n" +
              "This might provide better results, but also potentially increase analysis time a lot and is not recommended."
          ,
          "Analysis Script", AllIcons.Actions.Help);
    }
  };

  public static final MouseAdapter SHOW_HOW_TO_USE_LIST_OF_SOURCE_PATHS = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
      Messages.showMessageDialog("The script will start execution, if you edit any file in any directory from the list.\n\n" +
              "The plugin needs to decide if and which incremental script should be triggered if you edit some source file in your project.\n" +
          "If for example you edit a file in js/ folder, then you probably want to start the analysis for javascript only.\n" +
              "You also may have more then one source directory for javascript.\n" +
          "But if you edit a java file in src/main/java, then you want to start the analysis for java.\n" +
          "Depending on the situation you need to tell the script when to start.\n" +
          "For this example the script starts execution if you add src/main/java to the list and edit any file in that folder.\n",
          "Source Paths List", AllIcons.Actions.Help);
    }
  };

  private Project myProject;
  private JPanel myRootPanel;
  private JTextPane myScriptSourceTextPane;
  private JPanel myPanelForSourcePaths;
  private TextFieldWithBrowseButton myPathToSonarReportTextFieldWithBrowseButton;
  private JLabel labelForListOfSourcePaths;
  private JLabel labelForSourceCodeOfIncrementalAnalysisScript;
  private JLabel labelForPathToSonarReport;
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

    labelForListOfSourcePaths.setIcon(AllIcons.Actions.Help);
    labelForListOfSourcePaths.setHorizontalTextPosition(JLabel.LEFT);
    labelForListOfSourcePaths.addMouseListener(SHOW_HOW_TO_USE_LIST_OF_SOURCE_PATHS);

    labelForSourceCodeOfIncrementalAnalysisScript.setIcon(AllIcons.Actions.Help);
    labelForSourceCodeOfIncrementalAnalysisScript.setHorizontalTextPosition(JLabel.LEFT);
    labelForSourceCodeOfIncrementalAnalysisScript.addMouseListener(SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT);

    labelForPathToSonarReport.setIcon(AllIcons.Actions.Help);
    labelForPathToSonarReport.setHorizontalTextPosition(JLabel.LEFT);
    labelForPathToSonarReport.addMouseListener(SHOW_HOW_TO_DEFINE_SOURCE_CODE_OF_INCREMENTAL_ANALYSIS_SCRIPT);

    myPanelForSourcePaths.setLayout(new BorderLayout());
    myPanelForSourcePaths.add(createScriptSourcePathsTable(), BorderLayout.CENTER);

    myPathToSonarReportTextFieldWithBrowseButton.addActionListener(new ExternalEditorPathActionListener());

    return myRootPanel;
  }

  private JComponent createScriptSourcePathsTable() {
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
              public void consume(final java.util.List<VirtualFile> selectedFiles) {
                Set<VirtualFile> currentFiles = ImmutableSet.copyOf(mySourcePathsTable.getItems());
                Set<VirtualFile> newFilesSelection = ImmutableSet.<VirtualFile>builder()
                    .addAll(currentFiles)
                    .addAll(selectedFiles)
                    .build();
                mySourcePathsTable.setModelAndUpdateColumns(
                    new ListTableModel<VirtualFile>(
                        new ColumnInfo[]{SOURCE_PATH_COLUMN},
                        Lists.newArrayList(newFilesSelection),
                        0));
              }
            });
          }
        }).
        setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            TableUtil.removeSelectedItems(mySourcePathsTable);
          }
        }).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 100));
    return panelForTable;
  }


  public IncrementalScriptBean getIncrementalScriptBean() {
    return IncrementalScriptBean.of(
        FluentIterable.from(mySourcePathsTable.getItems())
            .transform(new Function<VirtualFile, String>() {
              @Override
              public String apply(VirtualFile virtualFile) {
                return virtualFile.getPath();
              }
            }).toList(),
        myScriptSourceTextPane.getText(),
        myPathToSonarReportTextFieldWithBrowseButton.getText()
    );
  }

  public void setValuesFrom(IncrementalScriptBean selectedIncrementalScriptBean) {
    mySourcePathsTable.setModelAndUpdateColumns(
        new ListTableModel<VirtualFile>(
            new ColumnInfo[]{SOURCE_PATH_COLUMN},
            Lists.newArrayList(FluentIterable.from(selectedIncrementalScriptBean.getSourcePaths())
                .transform(new Function<String, VirtualFile>() {
                  @Override
                  public VirtualFile apply(String path) {
                    return LocalFileSystem.getInstance().findFileByPath(path);
                  }
                })),
            0));
    myScriptSourceTextPane.setText(selectedIncrementalScriptBean.getSourceCodeOfScript());
    myPathToSonarReportTextFieldWithBrowseButton.setText(selectedIncrementalScriptBean.getPathToSonarReport());
  }

  private final class ExternalEditorPathActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      Application application = ApplicationManager.getApplication();
      VirtualFile previous = application.runWriteAction(new NullableComputable<VirtualFile>() {
        public VirtualFile compute() {
          final String path = FileUtil.toSystemIndependentName(myPathToSonarReportTextFieldWithBrowseButton.getText());
          return ! StringUtil.isEmptyOrSpaces(path) ? LocalFileSystem.getInstance().refreshAndFindFileByPath(path) : null ;
        }
      });
      FileChooserDescriptor fileDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
      fileDescriptor.setShowFileSystemRoots(true);
      fileDescriptor.setTitle("Configure Path");
      fileDescriptor.setDescription("Configure path to sonar-report.json");
      FileChooser.chooseFiles(
          fileDescriptor,
          myProject,
          Optional.fromNullable(previous).or(myProject.getBaseDir()),
          new Consumer<java.util.List<VirtualFile>>() {
            @Override
            public void consume(final java.util.List<VirtualFile> files) {
              String path = files.get(0).getPath();
              myPathToSonarReportTextFieldWithBrowseButton.setText(path);
            }
          }
      );
    }
  }

}
