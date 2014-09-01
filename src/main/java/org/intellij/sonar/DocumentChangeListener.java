package org.intellij.sonar;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.intellij.sonar.analysis.SonarExternalAnnotator;
import org.intellij.sonar.index2.IssuesByFileIndex;
import org.intellij.sonar.util.Finders;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.google.common.base.Optional.fromNullable;

public class DocumentChangeListener extends AbstractProjectComponent {

  public static final Set<VirtualFile> CHANGED_FILES = new CopyOnWriteArraySet<VirtualFile>();

  protected DocumentChangeListener(final Project project) {
    super(project);

    EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentAdapter() {

      @Override
      public void documentChanged(final DocumentEvent e) {
        rememberChangedFile(e);

        updateIssuesPositions(e, project);
      }
    });


  }

  private void updateIssuesPositions(final DocumentEvent e, final Project project) {
    final Optional<Document> document = fromNullable(e.getDocument());
    final List<Editor> editors = Finders.findEditorsFrom(document.get());
    for (final Editor editor : editors) {

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          // update issue line
          Set<RangeHighlighter> allHighlighters = Finders.findAllRangeHighlightersFrom(document.get());
          for (RangeHighlighter highlighter : allHighlighters) {
            Optional<Set<IssuesByFileIndex.MyIssue>> issues = fromNullable(highlighter.getUserData(SonarExternalAnnotator.KEY));
            if (!issues.isPresent()) continue;
            int ijLine = Finders.findLineOfRangeHighlighter(highlighter, editor);
            int rhLine = ijLine + 1;
            for (IssuesByFileIndex.MyIssue issue : issues.get()) {
              if (issue.line == null) continue;
              if (issue.line != rhLine) {
                issue.line = rhLine;
              }
            }
          }

          // remove issues without highlighter (highlighter was removed in editor)
          final Optional<VirtualFile> file = Optional.fromNullable(FileDocumentManager.getInstance().getFile(e.getDocument()));
          if (file.isPresent()) {
            Set<IssuesByFileIndex.MyIssue> issuesFromHighlighters = Sets.newLinkedHashSet();
            Set<RangeHighlighter> highlighters = Finders.findAllRangeHighlightersFrom(e.getDocument());
            for (RangeHighlighter highlighter : highlighters) {
              Optional<Set<IssuesByFileIndex.MyIssue>> issuesFromHighlighter = fromNullable(highlighter.getUserData(SonarExternalAnnotator.KEY));
              if (issuesFromHighlighter.isPresent()) {
                issuesFromHighlighters.addAll(issuesFromHighlighter.get());
              }
            }
            IssuesByFileIndex.index.put(file.get().getPath(), issuesFromHighlighters);

          }

          if (file.isPresent()) {
            Optional<PsiFile> psiFile = fromNullable(PsiManager.getInstance(project).findFile(file.get()));
            if (psiFile.isPresent()) {
              DaemonCodeAnalyzer.getInstance(project).restart(psiFile.get());
            }
          }

        }
      });
    }
  }

  private void rememberChangedFile(final DocumentEvent e) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runReadAction(new Runnable() {
          @Override
          public void run() {

            final Optional<VirtualFile> file = fromNullable(FileDocumentManager.getInstance().getFile(e.getDocument()));
            if (file.isPresent()) {
              CHANGED_FILES.add(file.get());
            }
          }
        });
      }
    });
  }
}
