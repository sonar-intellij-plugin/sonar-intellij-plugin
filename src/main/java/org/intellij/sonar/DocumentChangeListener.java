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
import org.intellij.sonar.util.Finders;

import java.util.Set;

public class DocumentChangeListener extends AbstractProjectComponent {

  protected DocumentChangeListener(Project project) {
    super(project);

    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentAdapter() {
      @Override
      public void beforeDocumentChange(DocumentEvent e) {
        super.beforeDocumentChange(e);
      }

      @Override
      public void documentChanged(final DocumentEvent e) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().runReadAction(new Runnable() {
              @Override
              public void run() {
                // for all highlighters
                final Set<RangeHighlighter> allRangeHighlighters = Finders.findAllRangeHighlightersFrom(e.getDocument());
                for (RangeHighlighter highlighter : allRangeHighlighters) {
                  for (Editor editor : EditorFactory.getInstance().getEditors(highlighter.getDocument())) {
                    final int intellijLine = Finders.findLineOfRangeHighlighter(highlighter, editor);
                    // get problem descriptors of highlighter
                    final Set<IssuesIndexEntry> issuesIndexEntries = highlighter.getUserData(SonarLocalInspectionTool.PROBLEM_DESCRIPTORS_KEY);
                    if (issuesIndexEntries != null) {
                      for (IssuesIndexEntry issuesIndexEntry : issuesIndexEntries) {
                        // update line of issue in issue index
                        int lineInEditor = intellijLine + 1; // of some reason intellij seams to count line beginning from 0
                        issuesIndexEntry.setLine(lineInEditor);
                      }
                    }
                  }
                }
              }
            });
          }
        });
      }
    });
  }

}
