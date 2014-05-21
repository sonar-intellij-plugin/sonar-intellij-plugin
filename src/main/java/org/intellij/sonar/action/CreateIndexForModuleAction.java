package org.intellij.sonar.action;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import org.intellij.sonar.analysis.FileNotInSourcePathException;
import org.intellij.sonar.analysis.IncrementalScriptProcess;
import org.intellij.sonar.analysis.NotificationManager;
import org.intellij.sonar.analysis.SonarLocalInspectionTool;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.index.*;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.sonarreport.data.SonarReport;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.services.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.fromNullable;

public class CreateIndexForModuleAction extends AnAction {

  private final static Logger LOG = Logger.getInstance(CreateIndexForModuleAction.class);
  private SonarConsole console;

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

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);
    if (module == null) {
      LOG.error(CreateIndexForModuleAction.class.getName() + "module specific action performed, but module is null");
      return;
    }
    console = SonarConsole.get(module.getProject());

    FileDocumentManager.getInstance().saveAllDocuments();

    final BackgroundTask backgroundTask = new BackgroundTask(module.getProject(), "Create IssuesIndex For Module " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND);
    backgroundTask.withModule(module).queue();
  }



  private class BackgroundTask extends Task.Backgroundable {
    private Module module;
    private ProgressIndicator indicator;

    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption) {
      super(project, title, canBeCancelled, backgroundOption);
    }

    public BackgroundTask withModule(Module module) {
      this.module = module;
      return this;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      this.indicator = indicator;
      doAction();
    }

    private void doAction() {

      final ImmutableList<VirtualFile> moduleFiles = getAllModuleFiles();

      DownloadSonarIssuesForModule downloadSonarIssuesForModule = new DownloadSonarIssuesForModule().invoke();
      ImmutableList<Resource> moduleSonarResources = downloadSonarIssuesForModule.getModuleSonarResources();
      ImmutableList<Issue> issuesFromSonarServer = downloadSonarIssuesForModule.getIssuesFromSonarServer();
      Optional<ModuleSettingsBean> moduleSettingsBean = downloadSonarIssuesForModule.getModuleSettingsBean();

      final IndexComponent indexComponent = createIndexForModule(moduleFiles, moduleSonarResources, issuesFromSonarServer);

      if (moduleSettingsBean.isPresent()) {
        final Collection<IncrementalScriptBean> incrementalScriptBeans = moduleSettingsBean.get().getScripts();
        final String workingDirectory = module.getProject().getBaseDir().getPath();
        for (IncrementalScriptBean incrementalScriptBean : incrementalScriptBeans) {
          final String sourceCodeOfScript = incrementalScriptBean.getSourceCodeOfScript();
          try {
            indicator.setText(String.format("Executing %s in %s", incrementalScriptBean.getSourceCodeOfScript(), workingDirectory));
            final int exitCode = IncrementalScriptProcess.of(incrementalScriptBean, workingDirectory, getProject())
                .exec()
                .waitFor();
            if (0 == exitCode) {
              createIndexFromSonarReport(moduleFiles, moduleSonarResources, indexComponent, incrementalScriptBean);
              SonarLocalInspectionTool.refreshInspectionsInEditor(myProject);
              // we have just freshly updated the index, clear all changed files
              myProject.getComponent(ChangedFilesComponent.class).changedFiles.clear();
            }
          } catch (IOException e) {
            console.error(String.format("Cannot execute %s\nRoot cause:\n\n%s", sourceCodeOfScript, e.getMessage()));
          } catch (InterruptedException e) {
            console.error(String.format("Interrupted execution of %s\nRoot cause:\n\n%s", sourceCodeOfScript, e.getMessage()));
          } catch (FileNotInSourcePathException ignore) {
            // script execution is not based on a changed file, skip
          }

        }
      }

    }

    private void createIndexFromSonarReport(ImmutableList<VirtualFile> moduleFiles, ImmutableList<Resource> moduleSonarResources, IndexComponent indexComponent, IncrementalScriptBean incrementalScriptBean) throws IOException {
      final String createIndexMessage = String.format("Create index for module %s from %s", module.getName(), incrementalScriptBean.getPathToSonarReport());
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
          IssuesIndexMerge.from(indexComponent.getState())
              .with(issuesFromSonarReport)
              .get();
      indexComponent.loadState(newIndexMapFromSonarReport);
      myProject.getComponent(NotificationManager.class).showNotificationFor(issuesFromSonarReport);
      console.info(String.format("Created index for module %s from %s in %d ms"
          , module.getName()
          , incrementalScriptBean.getPathToSonarReport()
          , stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)
          ));
    }

    private IndexComponent createIndexForModule(ImmutableList<VirtualFile> moduleFiles, ImmutableList<Resource> moduleSonarResources, ImmutableList<Issue> issuesFromSonarServer) {
      final String creatingIndexFromSonarServerMessage = String.format("Create index for module %s from sonar server", module.getName());
      indicator.setText(creatingIndexFromSonarServerMessage);
      console.info(creatingIndexFromSonarServerMessage);

      Stopwatch stopwatch = new Stopwatch().start();
      final Indexer indexer = new Indexer(moduleFiles, moduleSonarResources);
      final Map<IssuesIndexKey, Set<IssuesIndexEntry>> indexMapFromSonarServer = indexer.withSonarServerIssues(issuesFromSonarServer).create();

      final Optional<IndexComponent> indexComponent = IndexComponent.getInstance(module.getProject());
      final IssuesIndex issuesIndexFromSonarServer = new IssuesIndex(indexMapFromSonarServer);

      indicator.setText(String.format("Merging index with issues from sonar server"));
      final Map<IssuesIndexKey, Set<IssuesIndexEntry>> newIndexMapFromSonarServer =
          IssuesIndexMerge.from(indexComponent.get().getState())
              .with(issuesIndexFromSonarServer.getIndexValue())
              .get();

      indexComponent.get().loadState(newIndexMapFromSonarServer);
      console.info(String.format("Created index for module %s from sonar server in %d ms"
      , module.getName(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
      return indexComponent.get();
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

    private class DownloadSonarIssuesForModule {
      private ImmutableList<Issue> issuesFromSonarServer;
      private ImmutableList<Resource> moduleSonarResources;
      private Optional<ModuleSettingsBean> moduleSettingsBean;

      public ImmutableList<Issue> getIssuesFromSonarServer() {
        return issuesFromSonarServer;
      }

      public ImmutableList<Resource> getModuleSonarResources() {
        return moduleSonarResources;
      }

      public Optional<ModuleSettingsBean> getModuleSettingsBean() {
        return moduleSettingsBean;
      }

      public DownloadSonarIssuesForModule invoke() {
        final String downloadingSonarIssuesMessage = String.format("Download sonar issues for module %s", module.getName());
        console.info(downloadingSonarIssuesMessage);
        indicator.setText(downloadingSonarIssuesMessage);

        // downloadSonarIssuesForModule
        issuesFromSonarServer = ImmutableList.of();
        moduleSonarResources = ImmutableList.of();
        final ModuleSettingsComponent moduleSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
        moduleSettingsBean = fromNullable(moduleSettingsComponent.getState());
        if (moduleSettingsBean.isPresent()) {
          moduleSonarResources = ImmutableList.copyOf(moduleSettingsBean.get().getResources());
          final Optional<String> properServerName = moduleSettingsBean.get().getProperServerName(module.getProject());
          if (properServerName.isPresent()) {
            final Optional<SonarServerConfigurationBean> sonarServerConfigurationBeanOptional = SonarServersComponent.get(properServerName.get());
            if (sonarServerConfigurationBeanOptional.isPresent()) {
              Stopwatch stopwatch = new Stopwatch().start();
              final SonarServer sonarServer = SonarServer.create(sonarServerConfigurationBeanOptional.get());
              final ImmutableList.Builder<Issue> allIssuesBuilder = ImmutableList.builder();
              for (Resource moduleSonarResource : moduleSonarResources) {
                final ImmutableList<Issue> allIssuesForResource = sonarServer.getAllIssuesFor(moduleSonarResource.getKey());
                allIssuesBuilder.addAll(allIssuesForResource);
              }
              issuesFromSonarServer = allIssuesBuilder.build();
              console.info(String.format("Downloaded %d issues from %s in %d ms",
                  issuesFromSonarServer.size(),
                  sonarServerConfigurationBeanOptional.get().getName(),
                  stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
            }
          }

        }
        return this;
      }
    }
  }
}
