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
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;
import org.intellij.sonar.util.Finders;

import java.util.List;
import java.util.Map;
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

    @Override
    public void projectOpened() {
        super.projectOpened();
        CHANGED_FILES.clear();
    }

    @Override
    public void projectClosed() {
        super.projectClosed();
        CHANGED_FILES.clear();
    }

    private void updateIssuesPositions(final DocumentEvent e, final Project project) {
        final Optional<Document> document = fromNullable(e.getDocument());
        final List<Editor> editors = Finders.findEditorsFrom(document.get());
        for (final Editor editor : editors) {

            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {

                    updateIssueLines(document, editor);


                    final Optional<VirtualFile> file = removeIssuesDeletedInEditor(e, project);

                    updateHighlightingFor(file, project);

                }
            });
        }
    }

    private void updateHighlightingFor(Optional<VirtualFile> file, Project project) {
        if (file.isPresent() && !project.isDisposed() && project.isInitialized()) {
            Optional<PsiFile> psiFile = fromNullable(PsiManager.getInstance(project).findFile(file.get()));
            if (psiFile.isPresent()) {
                DaemonCodeAnalyzer.getInstance(project).restart(psiFile.get());
            }
        }
    }

    /**
     * removes issues without highlighter (highlighter was removed in editor) by iterating over all issues from
     * remaining highlighters and updating the issues index
     * @param documentEvent triggered the change of the document
     * @param project the document belongs to
     * @return
     */
    private Optional<VirtualFile> removeIssuesDeletedInEditor(DocumentEvent documentEvent, Project project) {
        final Optional<VirtualFile> file = Optional.fromNullable(
                FileDocumentManager.getInstance().getFile(
                        documentEvent.getDocument()));

        if (file.isPresent()) {
            Set<SonarIssue> issuesFromHighlighters = Sets.newLinkedHashSet();
            Set<RangeHighlighter> highlighters = Finders.findAllRangeHighlightersFrom(documentEvent.getDocument());
            retrieveIssuesFromHighlighters(issuesFromHighlighters, highlighters);

            final Optional<IssuesByFileIndexProjectComponent> indexComponent =
                    IssuesByFileIndexProjectComponent.getInstance(project);

            if (indexComponent.isPresent()) {
                final Map<String, Set<SonarIssue>> index = indexComponent.get().getIndex();
                index.put(file.get().getPath(), issuesFromHighlighters);
            }
        }
        return file;
    }

    private void retrieveIssuesFromHighlighters(
            Set<SonarIssue> issuesFromHighlighters,
            Set<RangeHighlighter> highlighters) {

        for (RangeHighlighter highlighter : highlighters) {
            Optional<Set<SonarIssue>> issuesFromHighlighter = fromNullable(
                    highlighter.getUserData(SonarExternalAnnotator.KEY));

            if (issuesFromHighlighter.isPresent()) {
                issuesFromHighlighters.addAll(issuesFromHighlighter.get());
            }
        }
    }

    /**
     * updates the line number of issues, if they are moved in the editor
     * @param document changed
     * @param editor containing the document
     */
    private void updateIssueLines(Optional<Document> document, Editor editor) {
        Set<RangeHighlighter> allHighlighters = Finders.findAllRangeHighlightersFrom(document.get());
        for (RangeHighlighter highlighter : allHighlighters) {
            updateLineForAllIssuesFrom(editor, highlighter);
        }
    }

    private void updateLineForAllIssuesFrom(Editor editor, RangeHighlighter highlighter) {
        Optional<Set<SonarIssue>> issues = fromNullable(highlighter.getUserData(SonarExternalAnnotator.KEY));
        if (!issues.isPresent()) return;
        int ijLine = Finders.findLineOfRangeHighlighter(highlighter, editor);
        int rhLine = ijLine + 1;
        for (SonarIssue issue : issues.get()) {
            if (issue.getLine() == null) continue;
            if (issue.getLine() != rhLine) {
                issue.setLine(rhLine);
            }
        }
    }

    private void rememberChangedFile(final DocumentEvent e) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        addFileFromEventToChangedFiles(e);
                    }
                });
            }
        });
    }

    private void addFileFromEventToChangedFiles(DocumentEvent e) {
        final Optional<VirtualFile> file = fromNullable(FileDocumentManager.getInstance().getFile(e.getDocument()));
        if (file.isPresent()) {
            CHANGED_FILES.add(file.get());
        }
    }
}
