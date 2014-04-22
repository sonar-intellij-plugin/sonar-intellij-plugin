package org.intellij.sonar.configuration.partials;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.TableUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.intellij.sonar.configuration.IncrementalScriptConfigurable;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class IncrementalAnalysisScriptsTableView {

  private final TableView<IncrementalScriptBean> myIncrementalAnalysisScriptsTable;
  private final Project myProject;
  private final JComponent myJComponent;

  public IncrementalAnalysisScriptsTableView(Project project) {
    this.myIncrementalAnalysisScriptsTable = new TableView<IncrementalScriptBean>();
    this.myProject = project;
    this.myJComponent = createJComponent();
  }

  private static final int SOURCE_CODE_ENTRY_MAX_LENGTH = 100;
  private static final ColumnInfo<IncrementalScriptBean, String> SCRIPT_COLUMN = new ColumnInfo<IncrementalScriptBean, String>("Script") {

    @Nullable
    @Override
    public String valueOf(IncrementalScriptBean incrementalScriptBean) {
      final String sourceCodeOfScript = incrementalScriptBean.getSourceCodeOfScript();
      if (!StringUtil.isEmptyOrSpaces(sourceCodeOfScript) && sourceCodeOfScript.length() >= SOURCE_CODE_ENTRY_MAX_LENGTH) {
        return sourceCodeOfScript.substring(0, SOURCE_CODE_ENTRY_MAX_LENGTH) + "...";
      } else {
        return sourceCodeOfScript;
      }
    }

  };

  public void setModel(List<IncrementalScriptBean> incrementalScriptBeans) {
    myIncrementalAnalysisScriptsTable.setModelAndUpdateColumns(
        new ListTableModel<IncrementalScriptBean>(
            new ColumnInfo[]{SCRIPT_COLUMN},
            incrementalScriptBeans,
            0)
    );
  }

  private JComponent createJComponent() {
    JPanel panelForTable = ToolbarDecorator.createDecorator(myIncrementalAnalysisScriptsTable, null)
        .setAddAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            IncrementalScriptConfigurable dlg = new IncrementalScriptConfigurable(myProject);
            dlg.show();
            if (dlg.isOK()) {
              final List<IncrementalScriptBean> incrementalScriptBeans = Lists.newArrayList(
                  ImmutableList.<IncrementalScriptBean>builder()
                      .addAll(myIncrementalAnalysisScriptsTable.getListTableModel().getItems())
                      .add(dlg.getIncrementalScriptBean())
                      .build()
              );
              setModel(incrementalScriptBeans);
            }
          }
        })
        .setEditAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            IncrementalScriptConfigurable dlg = new IncrementalScriptConfigurable(myProject);
            dlg.setValuesFrom(myIncrementalAnalysisScriptsTable.getSelectedObject());
            dlg.show();
            if (dlg.isOK()) {
              final IncrementalScriptBean newIncrementalScriptBean = dlg.getIncrementalScriptBean();
              final IncrementalScriptBean selectedIncrementalScriptBean = myIncrementalAnalysisScriptsTable.getSelectedObject();

              final ArrayList<IncrementalScriptBean> incrementalScriptBeans = Lists.newArrayList(ImmutableList.<IncrementalScriptBean>builder()
                  .addAll(
                      FluentIterable.from(myIncrementalAnalysisScriptsTable.getListTableModel().getItems())
                          .filter(new Predicate<IncrementalScriptBean>() {
                            @Override
                            public boolean apply(IncrementalScriptBean it) {
                              return !it.equals(selectedIncrementalScriptBean);
                            }
                          })
                          .toList()
                  )
                  .add(newIncrementalScriptBean)
                  .build());

              setModel(incrementalScriptBeans);
            }
          }
        })
        .disableUpDownActions()
        .setRemoveAction(new AnActionButtonRunnable() {
          @Override
          public void run(AnActionButton anActionButton) {
            TableUtil.removeSelectedItems(myIncrementalAnalysisScriptsTable);
          }
        })
        .createPanel();
    panelForTable.setPreferredSize(new Dimension(-1, 100));
    return panelForTable;
  }

  public JComponent getComponent() {
    return myJComponent;
  }

  public TableView<IncrementalScriptBean> getTable() {
    return myIncrementalAnalysisScriptsTable;
  }
}
