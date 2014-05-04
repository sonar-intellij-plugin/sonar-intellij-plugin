package org.intellij.sonar.inspection;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoProcessor;
import com.intellij.codeInsight.daemon.impl.LocalInspectionsPass;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.FileChangeListener;
import org.intellij.sonar.SonarInspectionToolProvider;
import org.intellij.sonar.SonarSeverity;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.persistence.IndexComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Optional.fromNullable;
import static org.intellij.sonar.FileChangeListener.changedPsiFiles;

public abstract class SonarLocalInspectionTool extends LocalInspectionTool {

  private static final Logger LOG = Logger.getInstance(SonarLocalInspectionTool.class);

  @NotNull
  public static TextRange getTextRange(@NotNull Document document, int line) {
    int lineStartOffset = document.getLineStartOffset(line - 1);
    int lineEndOffset = document.getLineEndOffset(line - 1);
    return new TextRange(lineStartOffset, lineEndOffset);
  }

  private static boolean shouldReadFromIndexFor(PsiFile psiFile) {
    return !FileChangeListener.changedPsiFiles.contains(psiFile);
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

    final ImmutableSet<PsiFile> openPsiFiles = ApplicationManager.getApplication().runReadAction(new Computable<ImmutableSet<PsiFile>>() {
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

    changedPsiFiles.clear();
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

  @NotNull
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

  @Nullable
  public ProblemDescriptor[] myCheckFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, final boolean isOnTheFly) {

    VirtualFile virtualFile = psiFile.getVirtualFile();

    final Project project = psiFile.getProject();

    Optional<IndexComponent> indexComponent = fromNullable(ServiceManager.getService(project, IndexComponent.class));
    if (!indexComponent.isPresent()) {
      LOG.error(String.format("Cannot retrieve %s", IndexComponent.class.getSimpleName()));
      return null;
    }

    final PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());
    final Document document = documentManager.getDocument(psiFile.getContainingFile());
    if (document == null) {
      return null;
    }

    final Collection<ProblemDescriptor> problemDescriptors = new LinkedHashSet<ProblemDescriptor>();

    final Map<IssuesIndexKey, ? extends Set<IssuesIndexEntry>> indexMap = indexComponent.get().getIssuesIndex();
    final Optional<Set<IssuesIndexEntry>> issuesForFile = fromNullable(
        indexMap.get(new IssuesIndexKey(virtualFile.getPath(), isNew(), getRuleKey()))
    );
    if (issuesForFile.isPresent()) {
      for (IssuesIndexEntry issueForFile : issuesForFile.get()) {
        if (null != issueForFile && null != issueForFile.getLine() && issueForFile.getLine() <= document.getLineCount()) {
          TextRange textRange = getTextRange(document, issueForFile.getLine());
          final ProblemHighlightType problemHighlightType = sonarSeverityToProblemHighlightType(issueForFile.getSeverity());

          final ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(psiFile, textRange,
              String.format("[%s] %s", issueForFile.getSeverity(), issueForFile.getMessage()),
              problemHighlightType,
              false
          );
          problemDescriptors.add(problemDescriptor);
        }
      }
    }
    return problemDescriptors.toArray(new ProblemDescriptor[problemDescriptors.size()]);
  }

}
