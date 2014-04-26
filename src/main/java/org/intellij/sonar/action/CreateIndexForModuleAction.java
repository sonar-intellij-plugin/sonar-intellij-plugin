package org.intellij.sonar.action;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
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
import org.intellij.sonar.index.IssuesIndex;
import org.intellij.sonar.index.Indexer;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.services.Resource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Optional.fromNullable;

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

    final BackgroundTask backgroundTask = new BackgroundTask(module.getProject(), "Create IssuesIndex For Module " + module.getName(), true, PerformInBackgroundOption.ALWAYS_BACKGROUND);
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
    final Optional<ModuleSettingsBean> state = fromNullable(moduleSettingsComponent.getState());
    if (state.isPresent()) {
      moduleSonarResources = ImmutableList.copyOf(state.get().getResources());
      final Optional<String> properServerName = state.get().getProperServerName(module.getProject());
      if (properServerName.isPresent()) {
        final Optional<SonarServerConfigurationBean> sonarServerConfigurationBeanOptional = SonarServersComponent.get(properServerName.get());
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

    }
    final Indexer indexer = new Indexer(moduleFiles, moduleSonarResources);
    final Map<IssuesIndexKey, Set<IssuesIndexEntry>> indexMapFromSonarServer = indexer.withSonarServerIssues(issuesFromSonarServer).create();

    final IndexComponent indexComponent = ServiceManager.getService(module.getProject(), IndexComponent.class);
    final IssuesIndex issuesIndexFromSonarServer = new IssuesIndex(indexMapFromSonarServer);
    final Map<IssuesIndexKey, Set<IssuesIndexEntry>> currentIssuesIndex = indexComponent.getIssuesIndex();

    final ImmutableSet<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>> currentIndexEntriesWithoutIssuesFromSonarServer =
        FluentIterable.from(currentIssuesIndex.entrySet())
        .filter(new Predicate<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>>() {
          @Override
          public boolean apply(Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndexKeySetEntry) {
            return !issuesIndexFromSonarServer.getIndexValue().containsKey(issuesIndexKeySetEntry.getKey());
          }
        }).toSet();
    final ImmutableSet<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>> currentIndexEntriesWithIssuesFromSonarServer =
        ImmutableSet.<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>>builder()
        .addAll(currentIndexEntriesWithoutIssuesFromSonarServer)
        .addAll(issuesIndexFromSonarServer.getIndexValue().entrySet())
        .build();
    final ConcurrentMap<IssuesIndexKey, Set<IssuesIndexEntry>> newIndexMap = Maps.newConcurrentMap();
    for(Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>> entry: currentIndexEntriesWithIssuesFromSonarServer) {
      newIndexMap.put(entry.getKey(), entry.getValue());
    }
    indexComponent.setIssuesIndex(newIndexMap);

//    createSonarReportForModule(module);
//    indexer.withSonarReportIssues(issuesFromSonarReport);
  }
}
