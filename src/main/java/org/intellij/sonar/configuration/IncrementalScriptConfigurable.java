package org.intellij.sonar.configuration;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class IncrementalScriptConfigurable extends DialogWrapper {
  private JPanel myRootPanel;
  private JTextPane myScriptSourceTextPane;
  private JPanel myPanelForSourcePathes;

  private final TableView<String> mySourcePathesTable;

  protected IncrementalScriptConfigurable(@Nullable Project project, TableView<String> sourcePathesTable) {
    super(project);
    this.mySourcePathesTable = sourcePathesTable;
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    myPanelForSourcePathes.setLayout(new BorderLayout());
    myPanelForSourcePathes.add(createScriptSourcePathesTable(), BorderLayout.CENTER);

    return myRootPanel;
  }

  private JComponent createScriptSourcePathesTable() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(mySourcePathesTable, null).
        setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {

          }
        }).
        disableUpDownActions().
        createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 100));
    return panelForTable;
  }

}
