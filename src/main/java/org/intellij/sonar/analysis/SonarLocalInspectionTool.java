package org.intellij.sonar.analysis;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.SonarSeverity;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.persistence.ChangedFilesComponent;
import org.intellij.sonar.persistence.IndexComponent;
import org.intellij.sonar.util.Finders;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.google.common.base.Optional.fromNullable;
import static org.intellij.sonar.util.Finders.findFirstElementAtLine;

public abstract class SonarLocalInspectionTool extends LocalInspectionTool {

  private static final Logger LOG = Logger.getInstance(SonarLocalInspectionTool.class);
  public static final Key<Set<IssuesIndexEntry>> PROBLEM_DESCRIPTORS_KEY = new Key<Set<IssuesIndexEntry>>("issuesIndexEntries");

  @NotNull
  public static Optional<TextRange> getTextRange(@NotNull PsiFile psiFile, int line) {
    final Optional<PsiElement> element = findFirstElementAtLine(psiFile, line);
    if (element.isPresent()) {
      return fromNullable(element.get().getTextRange());
    } else {
      return Optional.absent();
    }
  }

  private static boolean shouldReadFromIndexFor(@NotNull PsiFile psiFile) {
    return !psiFile.getProject().getComponent(ChangedFilesComponent.class).changedFiles.contains(psiFile.getVirtualFile().getPath());
  }

  private static ProblemHighlightType sonarSeverityToProblemHighlightType(String sonarSeverity) {
    if (StringUtils.isBlank(sonarSeverity)) {
      return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
    } else {
      sonarSeverity = sonarSeverity.toUpperCase();
      if (SonarSeverity.BLOCKER.toString().equals(sonarSeverity) || SonarSeverity.CRITICAL.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.ERROR;
      } else if (SonarSeverity.MAJOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      } else if (SonarSeverity.INFO.toString().equals(sonarSeverity) || SonarSeverity.MINOR.toString().equals(sonarSeverity)) {
        return ProblemHighlightType.WEAK_WARNING;
      } else {
        return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      }
    }
  }

  public static void refreshInspectionsInEditor(final Project project) {

    /*final ImmutableSet<PsiFile> openPsiFiles = ApplicationManager.getApplication().runReadAction(new Computable<ImmutableSet<PsiFile>>() {
      @Override
      public ImmutableSet<PsiFile> compute() {
        final Optional<VirtualFile[]> openFiles = fromNullable(FileEditorManager.getInstance(project).getOpenFiles());
        if (!(openFiles.isPresent() && !(openFiles.get().length < 1))) {
          // nothing to do if no files are visible in editor
          return ImmutableSet.of();
        }
        return FluentIterable.from(ImmutableSet.copyOf(openFiles.get()))
            .transform(new Function<VirtualFile, PsiFile>() {
              @Override
              public PsiFile apply(VirtualFile virtualFile) {
                return PsiManager.getInstance(project).findFile(virtualFile);
              }
            }).toSet();
      }
    });

    final InspectionManagerEx managerEx = (InspectionManagerEx) InspectionManager.getInstance(project);
    final GlobalInspectionContextImpl context = managerEx.createNewGlobalContext(false);
    final Collection<Class<SonarLocalInspectionTool>> sonarInspectionClasses = SonarInspectionToolProvider.classes;
    final ImmutableList<LocalInspectionToolWrapper> sonarLocalInspectionTools = FluentIterable.from(sonarInspectionClasses)
        .transform(new Function<Class<SonarLocalInspectionTool>, SonarLocalInspectionTool>() {
          @Override
          public SonarLocalInspectionTool apply(Class<SonarLocalInspectionTool> sonarLocalInspectionToolClass) {
            try {
              return sonarLocalInspectionToolClass.newInstance();
            } catch (InstantiationException e) {
              LOG.error(e.getMessage());
            } catch (IllegalAccessException e) {
              LOG.error(e.getMessage());
            }
            return null;
          }
        }).filter(new Predicate<SonarLocalInspectionTool>() {
          @Override
          public boolean apply(SonarLocalInspectionTool sonarLocalInspectionTool) {
            return null != sonarLocalInspectionTool;
          }
        })
        .transform(new Function<SonarLocalInspectionTool, LocalInspectionToolWrapper>() {
          @Override
          public LocalInspectionToolWrapper apply(SonarLocalInspectionTool sonarLocalInspectionTool) {
            return new LocalInspectionToolWrapper(sonarLocalInspectionTool);
          }
        }).toList();

    for (final PsiFile psiFile : openPsiFiles) {

      final Optional<VirtualFile> virtualFile = fromNullable(psiFile.getVirtualFile());
      if (!virtualFile.isPresent()) continue;

      PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
      final Optional<Document> document = fromNullable(psiDocumentManager.getDocument(psiFile));
      if (!document.isPresent()) continue;

      final LocalInspectionsPass localInspectionsPass =
          new LocalInspectionsPass(psiFile, document.get(), 0, document.get().getTextLength(), LocalInspectionsPass.EMPTY_PRIORITY_RANGE, true,
              HighlightInfoProcessor.getEmpty());

      final Runnable inspect = new Runnable() {
        @Override
        public void run() {
          localInspectionsPass.doInspectInBatch(context, managerEx, sonarLocalInspectionTools);
          final java.util.List<HighlightInfo> infos = localInspectionsPass.getInfos();
          UpdateHighlightersUtil.setHighlightersToEditor(
              project,
              document.get(),
              0,
              document.get().getTextLength(),
              infos,
              null,
              0
          );
        }
      };

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
              ProgressManager.getInstance().executeProcessUnderProgress(inspect, new ProgressIndicatorBase());
            }
          });
        }
      });

    }

    project.getComponent(ChangedFilesComponent.class).changedFiles.clear();*/
  }

  @Nls
  @NotNull
  @Override
  abstract public String getGroupDisplayName();

  abstract public boolean isNew();

  @NotNull
  @Override
  public String getShortName() {
    return this.getDisplayName().replaceAll("[^a-zA-Z_0-9.-]", "");
  }

  @Nls
  @NotNull
  @Override
  abstract public String getDisplayName();

  @NotNull
  @Override
  abstract public String getStaticDescription();

  @NotNull
  abstract public String getRuleKey();

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  /*@NotNull
  @Override
  public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, final boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitFile(PsiFile psiFile) {
        // don't care about non physical files
        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null || ProjectFileIndex.SERVICE.getInstance(psiFile.getProject()).getContentRootForFile(virtualFile) == null) {
          return;
        }

        if (!shouldReadFromIndexFor(psiFile)) {
          // skip updating from index if an incremental script is running at the moment
          // this avoids showing of issues on wrong lines of code
          return;
        }

        addDescriptors(myCheckFile(psiFile, holder.getManager(), isOnTheFly));
      }

      private void addDescriptors(final ProblemDescriptor[] descriptors) {
        if (descriptors != null) {
          for (ProblemDescriptor descriptor : descriptors) {
            LOG.assertTrue(descriptor != null, SonarLocalInspectionTool.this.getClass().getName());
            // we do assertTrue above
            //noinspection ConstantConditions
            holder.registerProblem(descriptor);
          }
        }
      }
    };
  }
*/
  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, final boolean isOnTheFly) {

    VirtualFile virtualFile = psiFile.getVirtualFile();

    final Project project = psiFile.getProject();

    Optional<IndexComponent> indexComponent = Finders.findIndexComponent(project);
    if (!indexComponent.isPresent()) {
      LOG.error(String.format("Cannot retrieve %s", IndexComponent.class.getSimpleName()));
      return null;
    }

    final Optional<Document> document = Finders.findDocumentFromPsiFile(psiFile);
    if (!document.isPresent()) {
      return null;
    }

    final Collection<ProblemDescriptor> problemDescriptors = new LinkedHashSet<ProblemDescriptor>();

    final Map<IssuesIndexKey, ? extends Set<IssuesIndexEntry>> indexMap = indexComponent.get().getIssuesIndex();
    final Optional<Set<IssuesIndexEntry>> issuesForFile = fromNullable(
        indexMap.get(new IssuesIndexKey(virtualFile.getPath(), isNew(), getRuleKey()))
    );
    if (issuesForFile.isPresent()) {
      for (final IssuesIndexEntry issueForFile : issuesForFile.get()) {
        if (null != issueForFile && null != issueForFile.getLine() && issueForFile.getLine() <= document.get().getLineCount()) {
          final Optional<TextRange> textRange = getTextRange(psiFile, issueForFile.getLine());
          if (textRange.isPresent()) {
            final ProblemHighlightType problemHighlightType = sonarSeverityToProblemHighlightType(issueForFile.getSeverity());
            final ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(psiFile, textRange.get(),
                String.format("[%s] %s", issueForFile.getSeverity(), issueForFile.getMessage()),
                problemHighlightType,
                false
            );
            problemDescriptors.add(problemDescriptor);

            final List<Editor> editors = Finders.findEditorsFrom(document.get());
            // in most cases there is only one editor for a file
            for (final Editor editor: editors) {
              final LogicalPosition logicalPosition = editor.offsetToLogicalPosition(findFirstElementAtLine(psiFile, issueForFile.getLine()).get().getTextOffset());
              final int lineEndOffset = document.get().getLineEndOffset(logicalPosition.line);
              final MarkupModel markupModel = editor.getMarkupModel();

              ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                  final Optional<RangeHighlighter> rangeHighlighterAtLine = Finders.findRangeHighlighterAtLine(editor, issueForFile.getLine());
                  if (rangeHighlighterAtLine.isPresent()) {
                    // user data is instantiated some lines below
                    //noinspection ConstantConditions
                    rangeHighlighterAtLine.get().getUserData(PROBLEM_DESCRIPTORS_KEY).add(issueForFile);
                  } else {
                    final RangeHighlighter rangeHighlighter = markupModel.addRangeHighlighter(
                        textRange.get().getStartOffset(),
                        lineEndOffset,
                        0,
                        null,
                        HighlighterTargetArea.EXACT_RANGE);
                    final Key<Set<IssuesIndexEntry>> descriptorsKey = PROBLEM_DESCRIPTORS_KEY;
                    rangeHighlighter.putUserData(descriptorsKey, Sets.newHashSet(issueForFile));
                  }
                }
              });
            }
          }
        }
      }
    }
    return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
  }

}
