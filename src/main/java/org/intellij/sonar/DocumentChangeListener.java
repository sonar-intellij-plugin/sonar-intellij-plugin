package org.intellij.sonar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.analysis.SonarLocalInspectionTool;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.util.Finders;

import java.util.Map;
import java.util.Set;

public class DocumentChangeListener extends AbstractProjectComponent {

  protected DocumentChangeListener(final Project project) {
    super(project);

    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentAdapter() {

      private Set<RangeHighlighter> highlightersBeforeChange;

      @Override
      public void documentChanged(final DocumentEvent e) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {

                // TODO: this approach did not work
               /* // TODO: remove removed highlighters and index entries if they were removed in editor
                // if lineOfChangedFragment == lineOfHighlighter
                //   && newFragment.isEmptyString
                //   && oldFragment.contains(oldSourceCode)
                // then
                //   remove the highlighter
                //   remove index entries of highlighter from index
                Set<RangeHighlighter> highlightersToBeRemoved = Sets.newHashSet();
                for (RangeHighlighter highlighter : Finders.findAllRangeHighlightersFrom(e.getDocument())) {
                  for (Editor editor : EditorFactory.getInstance().getEditors(highlighter.getDocument())) {
                    final int intellijLineOfChangedFragment = editor.offsetToLogicalPosition(e.getOffset()).line;
                    final int intellijLineOfHighlighter = Finders.findLineOfRangeHighlighter(highlighter, editor);
                    final String oldSourceCode = highlighter.getUserData(SonarLocalInspectionTool.OLD_SOURCE_CODE);
                    final String sourceCode = highlighter.getUserData(SonarLocalInspectionTool.SOURCE_CODE);
                    if (intellijLineOfChangedFragment == intellijLineOfHighlighter
                        && StringUtil.isEmptyOrSpaces(e.getNewFragment().toString())
                        && !StringUtil.isEmptyOrSpaces(sourceCode)
                        && e.getOldFragment().toString().contains(sourceCode)) {

                      //   remove index entries of highlighter from index
                      final Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndexOfHighlighter = highlighter.getUserData(SonarLocalInspectionTool.ISSUES_INDEX);
                      if (issuesIndexOfHighlighter != null) {
                        final Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex = Finders.findIssuesIndex(project);
                        for (IssuesIndexKey issuesIndexKeyOfHighlighter : issuesIndexOfHighlighter.keySet()) {
                          issuesIndex.remove(issuesIndexKeyOfHighlighter);
                        }
                      }

                      highlightersToBeRemoved.add(highlighter);

                    }
                  }
                }
                for (RangeHighlighter highlighter : highlightersToBeRemoved) {
                  for (Editor editor : EditorFactory.getInstance().getEditors(highlighter.getDocument())) {
                    editor.getMarkupModel().removeHighlighter(highlighter);
                  }
                }*/

                // update index entries based on highlighters position
                for (RangeHighlighter highlighter : Finders.findAllRangeHighlightersFrom(e.getDocument())) {
                  for (Editor editor : EditorFactory.getInstance().getEditors(highlighter.getDocument())) {

                    final int intellijLineOfHighlighter = Finders.findLineOfRangeHighlighter(highlighter, editor);
                    // get problem descriptors of highlighter
                    final Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex = highlighter.getUserData(SonarLocalInspectionTool.ISSUES_INDEX);
                    final String sourceCode = highlighter.getUserData(SonarLocalInspectionTool.SOURCE_CODE);
                    final String oldSourceCode = highlighter.getUserData(SonarLocalInspectionTool.OLD_SOURCE_CODE);
                    if (issuesIndex != null) {
                      for (Set<IssuesIndexEntry> values : issuesIndex.values()) {
                        for (IssuesIndexEntry issuesIndexEntry : values) {
                          // update line of issue in issue index
                          int lineInEditor = intellijLineOfHighlighter + 1; // intellij counts the lines beginning from 0
                          if (issuesIndexEntry.getLine() != null && lineInEditor != issuesIndexEntry.getLine()) {
                            issuesIndexEntry.setLine(lineInEditor);
                          }
                        }
                      }
                    }
                  }
                }

                // TODO: remove issue index entries without highlighters
                // assumption is that highlighters are removed if we remove the corresponding source code in editor
              }
            });
          }
        });
      }
    });
  }

}
