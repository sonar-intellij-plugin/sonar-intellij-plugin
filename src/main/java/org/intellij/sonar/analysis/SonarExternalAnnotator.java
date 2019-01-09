package org.intellij.sonar.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.UIUtil;
import com.intellij.xml.util.XmlStringUtil;
import org.intellij.sonar.DocumentChangeListener;
import org.intellij.sonar.index.IssuesByFileIndex;
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.util.Finders;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SonarExternalAnnotator
  extends ExternalAnnotator<SonarExternalAnnotator.InitialInfo,SonarExternalAnnotator.AnnotationResult> {

  public static final Key<Set<SonarIssue>> KEY = new Key<>("issues");

  public static class InitialInfo {
    public PsiFile psiFile;
  }

  public static class AnnotationResult {
    public Set<SonarIssue> sonarIssues = new HashSet<>();
  }

  @Nullable
  @Override
  public InitialInfo collectInformation(@NotNull PsiFile file) {
    InitialInfo initialInfo = new InitialInfo();
    initialInfo.psiFile = file;
    return initialInfo;
  }

  @Nullable
  @Override
  public AnnotationResult doAnnotate(final InitialInfo initialInfo) {
    final AnnotationResult annotationResult = new AnnotationResult();
      try {
          ApplicationManager.getApplication().executeOnPooledThread(
              () -> {
                annotationResult.sonarIssues = createSonarIssues(initialInfo.psiFile);
              }
          ).get();
      } catch (InterruptedException | ExecutionException e) {
          throw new IllegalStateException(e);
      }
    return annotationResult;
  }

  @NotNull
  private Set<SonarIssue> createSonarIssues(@NotNull PsiFile psiFile) {
    final Set<SonarIssue> issues;
    if (!DocumentChangeListener.CHANGED_FILES.contains(psiFile.getVirtualFile())) {
      issues = IssuesByFileIndex.getIssuesForFile(psiFile);
      for (SonarIssue issue : issues) {
        final TextRange textRange = Finders.getLineRange(psiFile,issue.getLine());
        createInvisibleHighlighter(psiFile,issue,textRange);
      }
    } else {
      final Set<SonarIssue> issuesFromHighlighters = Sets.newLinkedHashSet();
      Optional<Document> document = Finders.findDocumentFromPsiFile(psiFile);
      if (document.isPresent()) {
        Set<RangeHighlighter> highlighters = Finders.findAllRangeHighlightersFrom(document.get());
        for (RangeHighlighter highlighter : highlighters) {
          Optional<Set<SonarIssue>> issuesFromHighlighter = Optional.ofNullable(highlighter.getUserData(KEY));
          issuesFromHighlighter.ifPresent(issuesFromHighlighters::addAll);
        }
      }
      issues = issuesFromHighlighters;
    }
    return issues;
  }

  private void createInvisibleHighlighter(PsiFile psiFile,final SonarIssue issue,final TextRange textRange) {
    final Optional<Document> document = Finders.findDocumentFromPsiFile(psiFile);
    final List<Editor> editors = Finders.findEditorsFrom(document.get());
    for (final Editor editor : editors) {
      final MarkupModel markupModel = editor.getMarkupModel();
      ApplicationManager.getApplication().invokeLater(
              () -> {
                final Optional<RangeHighlighter> rangeHighlighterAtLine = Finders.findRangeHighlighterAtLine(
                        editor,
                        issue.getLine()
                );
                if (rangeHighlighterAtLine.isPresent()) {
                  final Set<SonarIssue> issuesOfHighlighter = rangeHighlighterAtLine.get().getUserData(KEY);
                  if (null != issuesOfHighlighter) {
                    issuesOfHighlighter.add(issue);
                  }
                } else {
                  TextAttributes attrs = new TextAttributes();
                  final RangeHighlighter rangeHighlighter = markupModel.addRangeHighlighter(
                          textRange.getStartOffset(),
                          textRange.getEndOffset(),
                          0,
                          attrs,
                          HighlighterTargetArea.EXACT_RANGE
                  );
                  Set<SonarIssue> issuesOfHighlighter = Sets.newLinkedHashSet();
                  issuesOfHighlighter.add(issue);
                  rangeHighlighter.putUserData(KEY,issuesOfHighlighter);
                }
              }
      );
    }
  }

  @Override
  public void apply(
    @NotNull final PsiFile file,
    final AnnotationResult annotationResult,
    @NotNull final AnnotationHolder holder
  ) {
    if (
      null == file.getVirtualFile() ||
        null == ProjectFileIndex.SERVICE.getInstance(file.getProject()).getContentRootForFile(file.getVirtualFile()) ||
        (
          // Fixes #106: Annotations in PHPStorm shown twice per File
          "HTML".equals(file.getFileType().getName()) &&
            "php".equals(file.getVirtualFile().getExtension()))
      ) {
      return;
    }
    createAnnotations(file,annotationResult,holder);
  }

  private void createAnnotations(
    @NotNull final PsiFile psiFile,
    AnnotationResult annotationResult,
    @NotNull AnnotationHolder holder
  ) {
    final Set<SonarIssue> issues = annotationResult.sonarIssues;
    for (SonarIssue issue : issues) {
      Optional<Annotation> annotation = createAnnotation(holder,psiFile,issue);
      if (annotation.isPresent()) {
        String tooltip = createTooltip(issue);
        annotation.get().setTooltip(tooltip);
      }
    }
  }

  public static Optional<Annotation> createAnnotation(AnnotationHolder holder,PsiFile psiFile,SonarIssue issue) {
    HighlightSeverity severity = SonarToIjSeverityMapping.toHighlightSeverity(issue.getSeverity());
    Annotation annotation;
    if (issue.getLine() == null) {
      annotation = createAnnotation(holder,issue.formattedMessage(),psiFile,severity);
      annotation.setFileLevelAnnotation(true);
    } else {
      Optional<PsiElement> startElement = Finders.findFirstElementAtLine(psiFile,issue.getLine());
      if (!startElement.isPresent()) {
        // There is no AST element on this line. Maybe a tabulation issue on a blank line?
        annotation = createAnnotation(
          holder,
          issue.formattedMessage(),
          Finders.getLineRange(psiFile,issue.getLine()),
          severity
        );
      } else
        if (startElement.get().isValid()) {
          TextRange lineRange = Finders.getLineRange(startElement.get());
          annotation = createAnnotation(holder,issue.formattedMessage(),lineRange,severity);
        } else {
          annotation = null;
        }
    }
    return Optional.ofNullable(annotation);
  }

  private static Annotation createAnnotation(
          AnnotationHolder holder,
          String message,
          PsiElement location,
          HighlightSeverity severity
  ) {
    if (HighlightSeverity.ERROR.equals(severity)) {
      return holder.createErrorAnnotation(location.getTextRange(),message);
    } else
    if (HighlightSeverity.WEAK_WARNING.equals(severity)) {
      return holder.createWeakWarningAnnotation(location.getTextRange(),message);
    } else
    if (HighlightSeverity.WARNING.equals(severity)) {
      return holder.createWarningAnnotation(location.getTextRange(),message);
    } else {
      throw new IllegalArgumentException("Unhandled severity "+severity);
    }
  }

  private static Annotation createAnnotation(
          AnnotationHolder holder,
          String message,
          TextRange textRange,
          HighlightSeverity severity
  ) {
    if (HighlightSeverity.ERROR.equals(severity)) {
      return holder.createErrorAnnotation(textRange,message);
    } else
    if (HighlightSeverity.WEAK_WARNING.equals(severity)) {
      return holder.createWeakWarningAnnotation(textRange,message);
    } else
    if (HighlightSeverity.WARNING.equals(severity)) {
      return holder.createWarningAnnotation(textRange,message);
    } else {
      throw new IllegalArgumentException("Unhandled severity "+severity);
    }
  }

  private static String createTooltip(SonarIssue issue) {
    String myShortcutText;
    final KeymapManager keymapManager = KeymapManager.getInstance();
    if (keymapManager != null) {
      final Keymap keymap = keymapManager.getActiveKeymap();
      myShortcutText = keymap == null
        ? ""
        : "("+KeymapUtil.getShortcutsText(keymap.getShortcuts(IdeActions.ACTION_SHOW_ERROR_DESCRIPTION))+")";
    } else {
      myShortcutText = "";
    }
    @NonNls final String link = " <a "
      +"href=\"#sonarissue/"+issue.getKey()+"\""
      +(UIUtil.isUnderDarcula()
      ? " color=\"7AB4C9\" "
      : "")
      +">"+DaemonBundle.message("inspection.extended.description")
      +"</a> "+myShortcutText;
    return XmlStringUtil.wrapInHtml(XmlStringUtil.escapeString(issue.formattedMessage())+link);
  }
}
