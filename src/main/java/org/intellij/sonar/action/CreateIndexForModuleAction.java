package org.intellij.sonar.action;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
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
import org.intellij.sonar.index.Index;
import org.intellij.sonar.index.Indexer;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.services.Resource;

public class CreateIndexForModuleAction extends AnAction {

  private final static Logger LOG = Logger.getInstance(CreateIndexForModuleAction.class);

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

  private class BackgroundTask extends Task.Backgroundable {
    private Module module;

    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption) {
      super(project, title, canBeCancelled, backgroundOption);
    }

    public BackgroundTask withModule(Module module) {
      this.module = module;
      return this;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      doAction(module);
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Module module = e.getData(DataKeys.MODULE);
    if (module == null) {
      LOG.error(CreateIndexForModuleAction.class.getName() + "module specific action performed, but module is null");
      return;
    }

    final BackgroundTask backgroundTask = new BackgroundTask(module.getProject(), "Create Index For Module " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND);
    backgroundTask.withModule(module).queue();
  }

  private void doAction(Module module) {
    // get all modules files
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
    final ImmutableList<VirtualFile> moduleFiles = moduleFilesBuilder.build();

    // downloadSonarIssuesForModule
    ImmutableList<Issue> issuesFromSonarServer = ImmutableList.of();
    ImmutableList<Resource> moduleSonarResources = ImmutableList.of();
    final ModuleSettingsComponent moduleSettingsComponent = module.getComponent(ModuleSettingsComponent.class);
    final ModuleSettingsBean state = moduleSettingsComponent.getState();
    if (state != null) {
      moduleSonarResources = ImmutableList.copyOf(state.getResources());
      final Optional<SonarServerConfigurationBean> sonarServerConfigurationBeanOptional = SonarServersService.get(state.getSonarServerName());
      if (sonarServerConfigurationBeanOptional.isPresent()) {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfigurationBeanOptional.get());
        final ImmutableList.Builder<Issue> allIssuesBuilder = ImmutableList.builder();
        for (Resource moduleSonarResource : moduleSonarResources) {
          final ImmutableList<Issue> allIssuesForResource = sonarServer.getAllIssuesFor(moduleSonarResource.getKey());
          allIssuesBuilder.addAll(allIssuesForResource);
        }
        issuesFromSonarServer = allIssuesBuilder.build();
      }

    }
    final Indexer indexer = new Indexer(moduleFiles, moduleSonarResources);
    final ImmutableMap<Index.Key, ImmutableSet<Index.Entry>> indexMapFromSonarServer = indexer.withSonarServerIssues(issuesFromSonarServer).create();

    final IndexComponent indexComponent = ServiceManager.getService(module.getProject(), IndexComponent.class);
    Index indexFromSonarServer = new Index(indexMapFromSonarServer);
    indexComponent.loadState(indexFromSonarServer);
//    createSonarReportForModule(module);
//    indexer.withSonarReportIssues(issuesFromSonarReport);
  }
}
