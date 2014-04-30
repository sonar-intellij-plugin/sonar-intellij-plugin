package org.intellij.sonar;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoProcessor;
import com.intellij.codeInsight.daemon.impl.LocalInspectionsPass;
import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.codeInspection.ex.InspectionManagerEx;
import com.intellij.codeInspection.ex.LocalInspectionToolWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressIndicatorBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.util.messages.MessageBusConnection;
import org.intellij.sonar.analysis.FileNotInSourcePathException;
import org.intellij.sonar.analysis.IncrementalScriptProcess;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.index.Indexer;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.index.IssuesIndexMerge;
import org.intellij.sonar.inspection.SonarLocalInspectionTool;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.intellij.sonar.persistence.IndexComponent;
import org.intellij.sonar.persistence.ModuleSettingsBean;
import org.intellij.sonar.persistence.ModuleSettingsComponent;
import org.intellij.sonar.sonarreport.data.SonarReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Resource;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.fromNullable;
import static org.intellij.sonar.FileChangeListener.changedPsiFiles;

public class FileSaveListener implements ApplicationComponent, BulkFileListener {

  public final static ConcurrentHashSet<IncrementalScriptBean> runningScripts = new ConcurrentHashSet<IncrementalScriptBean>();
  private final static Logger LOG = Logger.getInstance(FileSaveListener.class);
  public static volatile boolean isBlocked = false;
  private final MessageBusConnection connection;

  public FileSaveListener() {
    connection = ApplicationManager.getApplication().getMessageBus().connect();
  }

  /*private static List<ProblemDescriptor> runInspectionOnFile(@NotNull PsiFile file,
                                                             @NotNull LocalInspectionTool inspectionTool) {
    InspectionManagerEx inspectionManager = (InspectionManagerEx) InspectionManager.getInstance(file.getProject());
    GlobalInspectionContext context = inspectionManager.createNewGlobalContext(false);
    final List<ProblemDescriptor> problemDescriptors = InspectionEngine.runInspectionOnFile(file, new LocalInspectionToolWrapper(inspectionTool), context);
    return problemDescriptors;
  }*/

  @Override
  public void initComponent() {
    connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
  }

  @Override
  public void disposeComponent() {
    connection.disconnect();
  }

  @Override
  public void after(@NotNull List<? extends VFileEvent> events) {

    if (isBlocked) return;
    if (changedPsiFiles.isEmpty()) return;

    for (VFileEvent event : events) {
      final Optional<VirtualFile> file = fromNullable(event.getFile());
      tryToTriggerIncrementalAnalysisFor(file);
    }
  }

  public void tryToTriggerIncrementalAnalysisFor(Optional<VirtualFile> file) {
    if (!file.isPresent()) return;

    final Optional<Project> projectForFile = fromNullable(ProjectUtil.guessProjectForFile(file.get()));
    if (!projectForFile.isPresent()) return;

    final Optional<PsiFile> psiFile = fromNullable(PsiManager.getInstance(projectForFile.get()).findFile(file.get()));
    if (!psiFile.isPresent() || !changedPsiFiles.contains(psiFile.get())) return;

    final Optional<Module> moduleForFile = fromNullable(ModuleUtil.findModuleForFile(file.get(), projectForFile.get()));
    if (!moduleForFile.isPresent()) return;

    final ModuleSettingsComponent moduleSettingsComponent = moduleForFile.get().getComponent(ModuleSettingsComponent.class);
    final Optional<ModuleSettingsBean> moduleSettingsComponentState = fromNullable(moduleSettingsComponent.getState());
    if (!moduleSettingsComponentState.isPresent()) return;
    final Collection<IncrementalScriptBean> incrementalScriptBeans = moduleSettingsComponentState.get().getScripts();
    for (IncrementalScriptBean incrementalScriptBean : incrementalScriptBeans) {
      // TODO: restart the script if it is already running
      if (!runningScripts.contains(incrementalScriptBean)) {
        final BackgroundTask backgroundTask = new BackgroundTask(
            projectForFile.get(), "Executing incremental Analysis", true, PerformInBackgroundOption.ALWAYS_BACKGROUND, incrementalScriptBean, file.get());
        backgroundTask
            .withModule(moduleForFile.get())
            .queue();
        runningScripts.add(incrementalScriptBean);
      }
    }

  }


  @Override
  public void before(@NotNull List<? extends VFileEvent> events) {

  }

  @NotNull
  @Override
  public String getComponentName() {
    return FileSaveListener.class.getSimpleName();
  }

  private class BackgroundTask extends Task.Backgroundable {
    private final IncrementalScriptBean incrementalScriptBean;
    private final VirtualFile file;
    private final SonarConsole console;
    private ProgressIndicator indicator;
    private Module module;

    private BackgroundTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption, IncrementalScriptBean incrementalScriptBean, VirtualFile file) {
      super(project, title, canBeCancelled, backgroundOption);
      this.incrementalScriptBean = incrementalScriptBean;
      this.file = file;
      this.console = SonarConsole.get(project);
    }

    public BackgroundTask withModule(Module module) {
      this.module = module;
      return this;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      this.indicator = indicator;
      try {
        final String workingDirectory = getProject().getBaseDir().getPath();
        indicator.setText(String.format("Executing %s in %s", incrementalScriptBean.getSourceCodeOfScript(), workingDirectory));
        IncrementalScriptProcess.of(incrementalScriptBean, workingDirectory, getProject())
            .onChangeOf(file.getPath())
            .exec()
            .waitFor();
      } catch (IOException e) {
        console.error(String.format("Cannot execute %s\nRoot cause:\n\n%s", incrementalScriptBean.getSourceCodeOfScript(), e.getMessage()));
      } catch (InterruptedException e) {
        console.error(String.format("Interrupted execution of %s\nRoot cause:\n\n%s", incrementalScriptBean.getSourceCodeOfScript(), e.getMessage()));
      } catch (FileNotInSourcePathException ignore) {
        // don't execute anything is file is not in source paths
      } finally {
        runningScripts.remove(incrementalScriptBean);
      }

      final ModuleSettingsComponent moduleSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
      Optional<ModuleSettingsBean> moduleSettingsBean = fromNullable(moduleSettingsComponent.getState());
      if (moduleSettingsBean.isPresent()) {
        ImmutableList<Resource> moduleSonarResources = ImmutableList.copyOf(moduleSettingsBean.get().getResources());
        final IndexComponent indexComponent = ServiceManager.getService(module.getProject(), IndexComponent.class);
        try {
          // TODO: copy pasted the report logic from CreateIndexForModuleAction
          createIndexFromSonarReport(getAllModuleFiles(), moduleSonarResources, indexComponent, incrementalScriptBean);
        } catch (IOException e) {
          console.error(String.format("Cannot read sonar report from %s\nRoot cause: %s"
              , incrementalScriptBean.getPathToSonarReport()
              , e.getMessage()
          ));
        }
      }

      taskDone();
    }

    private void taskDone() {

      final InspectionManagerEx managerEx = (InspectionManagerEx) InspectionManager.getInstance(getProject());
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

      for (final PsiFile psiFile: changedPsiFiles) {

        final Optional<VirtualFile> virtualFile = fromNullable(psiFile.getVirtualFile());
        if (!virtualFile.isPresent()) continue;

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(getProject());
        final Optional<Document> document = fromNullable(psiDocumentManager.getDocument(psiFile));
        if (!document.isPresent()) continue;

        final LocalInspectionsPass localInspectionsPass =
            new LocalInspectionsPass(psiFile, document.get(), 0, document.get().getTextLength(), LocalInspectionsPass.EMPTY_PRIORITY_RANGE, true,
            HighlightInfoProcessor.getEmpty());

        final Runnable inspect = new Runnable() {
          @Override
          public void run() {
            localInspectionsPass.doInspectInBatch(context, managerEx, sonarLocalInspectionTools);
            final List<HighlightInfo> infos = localInspectionsPass.getInfos();

            UpdateHighlightersUtil.setHighlightersToEditor(
                getProject(),
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

    private ImmutableList<VirtualFile> getAllModuleFiles() {
      final ImmutableList.Builder<VirtualFile> moduleFilesBuilder = ImmutableList.builder();
      ModuleRootManager.getInstance(module).getFileIndex().iterateContent(new ContentIterator() {
        @Override
        public boolean processFile(VirtualFile fileOrDir) {
          if (fileOrDir.isDirectory()) {
            moduleFilesBuilder.addAll(getFilesFromDir(fileOrDir));
            return true;
          }
          return false;
        }
      });
      return moduleFilesBuilder.build();
    }

    private ImmutableList<VirtualFile> getFilesFromDir(VirtualFile dir) {
      if (!dir.isDirectory()) {
        return ImmutableList.of();
      } else {
        final ImmutableList.Builder<VirtualFile> filesInCurrentDir = ImmutableList.builder();
        VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
          @Override
          public boolean visitFile(@NotNull VirtualFile file) {
            if (!file.isDirectory()) {
              filesInCurrentDir.add(file);
            }
            return super.visitFile(file);
          }
        });
        return filesInCurrentDir.build();
      }
    }

    private void createIndexFromSonarReport(ImmutableList<VirtualFile> moduleFiles, ImmutableList<Resource> moduleSonarResources, IndexComponent indexComponent, IncrementalScriptBean incrementalScriptBean) throws IOException {
      final String createIndexMessage = String.format("Creating index for module %s from %s", module.getName(), incrementalScriptBean.getPathToSonarReport());
      console.info(createIndexMessage);
      indicator.setText(createIndexMessage);
      // read json report
      Stopwatch stopwatch = new Stopwatch().start();
      final String pathToSonarReport = incrementalScriptBean.getPathToSonarReport();
      String sonarReportContent = Files.toString(new File(pathToSonarReport), Charsets.UTF_8);
      final SonarReport sonarReport = SonarReport.fromJson(sonarReportContent);
      final Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesFromSonarReport = new Indexer(moduleFiles, moduleSonarResources)
          .withSonarReportIssues(sonarReport.getIssues())
          .create();
      // update index from json report
      indicator.setText(String.format("Merging index with issues from sonar report"));
      final Map<IssuesIndexKey, Set<IssuesIndexEntry>> newIndexMapFromSonarReport =
          IssuesIndexMerge.from(indexComponent.getIssuesIndex())
              .with(issuesFromSonarReport)
              .get();
      indexComponent.setIssuesIndex(newIndexMapFromSonarReport);
      console.info(String.format("Created index for module %s from %s in %d ms"
          , module.getName()
          , incrementalScriptBean.getPathToSonarReport()
          , stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)
      ));
    }
  }

}
