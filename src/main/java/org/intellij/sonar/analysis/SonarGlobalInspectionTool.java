package org.intellij.sonar.analysis;

import com.google.common.base.Optional;
import com.intellij.codeInspection.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ConcurrentHashSet;
import org.intellij.sonar.util.Finders;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SonarGlobalInspectionTool extends GlobalSimpleInspectionTool {


  /**
   * @see com.intellij.codeInspection.InspectionEP#groupDisplayName
   * @see com.intellij.codeInspection.InspectionEP#groupKey
   * @see com.intellij.codeInspection.InspectionEP#groupBundle
   */
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "TODO Sonar Group";
  }

  /**
   * @see com.intellij.codeInspection.InspectionEP#displayName
   * @see com.intellij.codeInspection.InspectionEP#key
   * @see com.intellij.codeInspection.InspectionEP#bundle
   */
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "TODO display name";
  }

  /**
   * Override this method to return a html inspection description. Otherwise it will be loaded from resources using ID.
   *
   * @return hard-code inspection description.
   */
  @Nullable
  @Override
  public String getStaticDescription() {
    return "TODO description";
  }

  private final static ConcurrentHashSet<RangeHighlighter> rangeHighlighters = new ConcurrentHashSet<RangeHighlighter>();

  @Override
  public void checkFile(@NotNull PsiFile psiFile, @NotNull InspectionManager manager, @NotNull ProblemsHolder problemsHolder, @NotNull GlobalInspectionContext globalContext, @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    // find element at a line

    if (psiFile.getVirtualFile().getNameWithoutExtension().equals("TranslationServiceImpl")) {
      final int fixedLineNumber = 41;
      final Optional<PsiElement> elementAtFirstLine = Finders.findFirstElementAtLine(psiFile, fixedLineNumber);
      final Optional<Document> document = Finders.findDocumentFromPsiFile(psiFile);

      if (document.isPresent() && elementAtFirstLine.isPresent()) {
        final List<Editor> editors = Finders.findEditorsFrom(document.get());
        for (Editor editor: editors) {
          final MarkupModel markupModel = editor.getMarkupModel();
          for (final RangeHighlighter rangeHighlighter: rangeHighlighters) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              @Override
              public void run() {
                markupModel.removeHighlighter(rangeHighlighter);
                rangeHighlighters.remove(rangeHighlighter);
              }
            });
          }
          final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(elementAtFirstLine.get().getTextOffset());
          final int lineEndOffset = document.get().getLineEndOffset(logicalPosition.line);

          // save highlighter to be able to remove it later
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              final RangeHighlighter rangeHighlighter = markupModel.addRangeHighlighter(
                  elementAtFirstLine.get().getTextRange().getStartOffset(),
                  lineEndOffset,
                  0,
                  null,
                  HighlighterTargetArea.EXACT_RANGE);
              rangeHighlighters.add(rangeHighlighter);
            }
          });


        }
      }
      // create manuel marking with popup, link for more... and strg+f1 shortcut

      // TODO: remove experimental code
    /*final Editor[] editors = EditorFactory.getInstance().getEditors(document);
    if (editors.length == 1) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final int fixedLineNumber = 5;
          final Editor editor = editors[0];
          editor.getMarkupModel();
          final MarkupModel markupModel = editor.getMarkupModel();
          TextAttributes textAttributes = new TextAttributes(Color.BLACK, Color.WHITE, Color.YELLOW, EffectType.WAVE_UNDERSCORE, 1);
          final RangeHighlighter lineHighlighter = markupModel.addLineHighlighter(fixedLineNumber, 1, textAttributes);
        }
      });*/

    }
  }
}
