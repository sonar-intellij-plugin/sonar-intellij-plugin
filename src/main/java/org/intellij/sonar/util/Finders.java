package org.intellij.sonar.util;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;

public class Finders {

  public static Optional<PsiElement> findFirstElementAtLine(@NotNull final PsiFile file, int line) {
    int ijLine = line - 1;
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    Optional<PsiElement> element = Optional.absent();
    try {
      if (document != null) {
        final int offset = document.getLineStartOffset(ijLine);
        element = fromNullable(file.getViewProvider().findElementAt(offset));
        if (element.isPresent() && document.getLineNumber(element.get().getTextOffset()) != ijLine) {
          element = fromNullable(element.get().getNextSibling());
        }
      }
    } catch (@NotNull final IndexOutOfBoundsException ignore) {
      // Ignore this exception
    }

    while (element.isPresent() && element.get().getTextLength() == 0) {
      element = fromNullable(element.get().getNextSibling());
    }

    return element;
  }

  public static Optional<Document> findDocumentFromPsiFile(PsiFile psiFile) {
    final Optional<Project> project = fromNullable(psiFile.getProject());
    if (!project.isPresent()) return Optional.absent();

    return fromNullable(PsiDocumentManager.getInstance(project.get()).getDocument(psiFile));
  }

  @NotNull
  public static List<Editor> findEditorsFrom(@NotNull Document document) {
    return Lists.newArrayList(EditorFactory.getInstance().getEditors(document));
  }

  @NotNull
  public static Optional<RangeHighlighter> findRangeHighlighterAtLine(final Editor editor, final int line) {
    final MarkupModel markupModel = editor.getMarkupModel();
    final RangeHighlighter[] highlighters = markupModel.getAllHighlighters();
    for (RangeHighlighter highlighter : highlighters) {
      final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(highlighter.getStartOffset());
      final int lineOfHighlighter = logicalPosition.line;
      if (lineOfHighlighter == line - 1) {
        return Optional.of(highlighter);
      }
    }

    return Optional.absent();
//    final HighlighterLineFinder finder = new HighlighterLineFinder(editor, line - 1);
//    ApplicationManager.getApplication().invokeLater(finder);
//    return fromNullable(finder.getFoundHighlighter());
  }

  @NotNull
  public static Set<RangeHighlighter> findAllRangeHighlightersFrom(@NotNull Document document) {
    final HashSet<RangeHighlighter> highlighters = Sets.newHashSet();
    for (Editor editor: findEditorsFrom(document)) {

      final RangeHighlighter[] highlightersFromCurrentEditor = editor.getMarkupModel().getAllHighlighters();
      highlighters.addAll(Sets.newHashSet(highlightersFromCurrentEditor));
    }
    return highlighters;
  }

  public static int findLineOfRangeHighlighter(@NotNull RangeHighlighter highlighter, @NotNull Editor editor) {
    final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(highlighter.getStartOffset());
    return logicalPosition.line;
  }

  private static class HighlighterLineFinder implements Runnable {
    private final MarkupModel markupModel;
    private final Editor editor;
    private final int line;
    private RangeHighlighter foundHighlighter;

    public HighlighterLineFinder(Editor editor, int line) {
      this.markupModel = editor.getMarkupModel();
      this.editor = editor;
      this.line = line;
    }

    @Override
    public void run() {
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        @Override
        public void run() {
          final RangeHighlighter[] highlighters = markupModel.getAllHighlighters();
          for (RangeHighlighter highlighter : highlighters) {
            final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(highlighter.getStartOffset());
            final int lineOfHighlighter = logicalPosition.line;
            if (lineOfHighlighter == line) {
              foundHighlighter = highlighter;
              break;
            }
          }
        }
      });
    }

    public RangeHighlighter getFoundHighlighter() {
      return foundHighlighter;
    }
  }
}
