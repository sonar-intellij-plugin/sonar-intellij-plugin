package org.intellij.sonar;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.util.containers.ConcurrentHashSet;
import org.intellij.sonar.analysis.FileNotInSourcePathException;
import org.intellij.sonar.analysis.IncrementalScriptProcess;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.index.Indexer;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.index.IssuesIndexMerge;
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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.fromNullable;

public class FileChangesListener extends AbstractProjectComponent{
  private final static Logger LOG = Logger.getInstance(FileChangesListener.class);
  public final static ConcurrentHashSet<IncrementalScriptBean> runningScripts = new ConcurrentHashSet<IncrementalScriptBean>();

  protected FileChangesListener(Project project) {
    super(project);
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      @Override
      public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
        super.beforeChildrenChange(event);
        final Optional<PsiFile> psiFile = fromNullable(event.getFile());
        if (!psiFile.isPresent()) return;
        final Optional<VirtualFile> virtualFile = fromNullable(psiFile.get().getVirtualFile());
        if (!virtualFile.isPresent() || virtualFile.get().isDirectory()) return;
        tryToTriggerIncrementalAnalysisFor(virtualFile);
      }
    });
  }

  public void tryToTriggerIncrementalAnalysisFor(Optional<VirtualFile> file) {
    final Optional<Project> projectForFile = fromNullable(ProjectUtil.guessProjectForFile(file.get()));
    if (!projectForFile.isPresent()) return;

    final Optional<Module> moduleForFile = fromNullable(ModuleUtil.findModuleForFile(file.get(), projectForFile.get()));
    if (!moduleForFile.isPresent()) return;

    final ModuleSettingsComponent moduleSettingsComponent = moduleForFile.get().getComponent(ModuleSettingsComponent.class);
    final Optional<ModuleSettingsBean> moduleSettingsComponentState = fromNullable(moduleSettingsComponent.getState());
    if (!moduleSettingsComponentState.isPresent()) return;
    final Collection<IncrementalScriptBean> incrementalScriptBeans = moduleSettingsComponentState.get().getScripts();
    for (IncrementalScriptBean incrementalScriptBean: incrementalScriptBeans) {
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

  public boolean shouldIndexBeUpdatedFor(Optional<VirtualFile> file) {
    final Optional<Project> projectForFile = fromNullable(ProjectUtil.guessProjectForFile(file.get()));
    if (!projectForFile.isPresent()) return false;
    final Optional<Module> moduleForFile = fromNullable(ModuleUtil.findModuleForFile(file.get(), projectForFile.get()));
    if (!moduleForFile.isPresent()) return false;

    final ModuleSettingsComponent moduleSettingsComponent = moduleForFile.get().getComponent(ModuleSettingsComponent.class);
    final Optional<ModuleSettingsBean> moduleSettingsComponentState = fromNullable(moduleSettingsComponent.getState());
    if (!moduleSettingsComponentState.isPresent()) return false;
    final Collection<IncrementalScriptBean> incrementalScriptBeans = moduleSettingsComponentState.get().getScripts();
    for (IncrementalScriptBean incrementalScriptBean: incrementalScriptBeans) {
      if (runningScripts.contains(incrementalScriptBean)) {
        return false;
      }
    }
    return true;
  }

  private class BackgroundTask extends Task.Backgroundable {
    private final IncrementalScriptBean incrementalScriptBean;
    private final VirtualFile file;
    private ProgressIndicator indicator;
    private final SonarConsole console;
    private Module module;

    private BackgroundTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption, IncrementalScriptBean incrementalScriptBean, VirtualFile file) {
      super(project, title, canBeCancelled, backgroundOption);
      this.incrementalScriptBean = incrementalScriptBean;
      this.file= file;
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
        LOG.error(String.format("Cannot execute %s\nRoot cause:\n\n%s", incrementalScriptBean.getSourceCodeOfScript(), e.getMessage()));
      } catch (InterruptedException e) {
        LOG.error(String.format("Interrupted execution of %s\nRoot cause:\n\n%s", incrementalScriptBean.getSourceCodeOfScript(), e.getMessage()));
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
          createIndexFromSonarReport(getAllModuleFiles(), moduleSonarResources, indexComponent, incrementalScriptBean );
        } catch (IOException e) {
          LOG.error(String.format("Cannot read sonar report from %s\nRoot cause: %s"
              , incrementalScriptBean.getPathToSonarReport()
              , e.getMessage()
          ));
        }
      }
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
