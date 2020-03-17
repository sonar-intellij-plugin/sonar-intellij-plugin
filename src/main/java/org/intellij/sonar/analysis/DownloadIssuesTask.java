package org.intellij.sonar.analysis;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.index.IssuesByFileIndexer;
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.sonarserver.SonarServer;
import org.intellij.sonar.util.DurationUtil;
import org.sonarqube.ws.Issues.Issue;

import java.util.AbstractCollection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadIssuesTask implements Runnable {

  private final SonarServerConfig sonarServerConfig;
  private final Set<String> resourceKeys;
  private final List<PsiFile> psiFiles;
  private final Map<String,ImmutableList<Issue>> downloadedIssuesByResourceKey = Maps.newConcurrentMap();
  private final SonarQubeInspectionContext.EnrichedSettings enrichedSettings;
  private final SonarConsole sonarConsole;

  private DownloadIssuesTask(
          SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
          SonarServerConfig sonarServerConfig,
          Set<String> resourceKeys,
          List<PsiFile> psiFiles
  ) {
    this.enrichedSettings = enrichedSettings;
    this.sonarServerConfig = sonarServerConfig;
    this.resourceKeys = resourceKeys;
    this.psiFiles = psiFiles;
    this.sonarConsole = SonarConsole.get(enrichedSettings.project);
  }

  public static Optional<DownloadIssuesTask> from(
    SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
    ImmutableList<PsiFile> psiFiles
  ) {
    return new DownloadIssuesTaskBuilder()
            .buildFrom(enrichedSettings, psiFiles)
            .maybeGetDownloadIssuesTask();
  }

  @Override
  public void run() {
    final SonarServer sonarServer = SonarServer.create(sonarServerConfig);
    final long startTime = System.currentTimeMillis();
    for (String resourceKey : resourceKeys) {
      final String downloadingIssuesMessage = String.format("Downloading issues for SonarQube resource %s",resourceKey);
      sonarConsole.info(downloadingIssuesMessage);
        final ImmutableList<Issue> issues;
        tryDownloadingIssues(sonarServer, resourceKey);
    }
    onSuccess(startTime);
  }

    private void tryDownloadingIssues(SonarServer sonarServer, String resourceKey) {
        ImmutableList<Issue> issues;
        try {
            issues = sonarServer.getAllIssuesFor(resourceKey, sonarServerConfig.getOrganization());
            downloadedIssuesByResourceKey.put(resourceKey,issues);
        } catch (Exception e) {
            sonarConsole.error(e.getMessage());
            Notifications.Bus.notify(
                    new Notification(
                            "SonarQube","SonarQube",
                            "Downloading sonar issues failed!", NotificationType.ERROR
                    ),enrichedSettings.project
            );
        }
    }

    private void onSuccess(long downloadStartTime) {
    final long downloadedIssuesCount = downloadedIssuesByResourceKey.values().stream()
            .mapToLong(AbstractCollection::size)
            .sum();
    sonarConsole.info(
      String.format(
        "Downloaded %d issues in %s",
        downloadedIssuesCount,
        DurationUtil.getDurationBreakdown(System.currentTimeMillis()-downloadStartTime)
      )
    );
    createIssuesIndex();
  }

  private void createIssuesIndex() {
    for (Map.Entry<String, ImmutableList<Issue>> entry : downloadedIssuesByResourceKey.entrySet()) {
      if (ProgressManager.getInstance().getProgressIndicator().isCanceled()) break;
      sonarConsole.info(String.format("Creating index for SonarQube resource %s",entry.getKey()));
      long indexCreationStartTime = System.currentTimeMillis();
      final ImmutableList<Issue> issues = entry.getValue();
      final Map<String, Set<SonarIssue>> index = new IssuesByFileIndexer(psiFiles)
        .withSonarServerIssues(issues)
        .withSonarConsole(sonarConsole)
        .create();
      final Optional<IssuesByFileIndexProjectComponent> indexComponent =
        IssuesByFileIndexProjectComponent.getInstance(enrichedSettings.project);
      indexComponent.ifPresent(issuesByFileIndexProjectComponent -> issuesByFileIndexProjectComponent.getIndex().putAll(index));
      final int issuesCountInIndex = (int) index.values().stream()
              .mapToLong(Set::size)
              .sum();
      sonarConsole.info(
        String.format(
          "Finished creating index with %d issues for SonarQube resource %s in %s",
          issuesCountInIndex,
          entry.getKey(),
          DurationUtil.getDurationBreakdown(System.currentTimeMillis()-indexCreationStartTime)
        )
      );
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(DownloadIssuesTask.class.getName())
      .add("sonarServerConfig",sonarServerConfig)
      .add("resourceKeys",resourceKeys)
      .add("psiFiles",psiFiles)
      .add("downloadedIssuesByResourceKey",downloadedIssuesByResourceKey)
      .toString();
  }

    private static class DownloadIssuesTaskBuilder {
        private DownloadIssuesTask downloadIssuesTask;
        private boolean processing;
        private String serverName;
        private Settings settings;
        private SonarServerConfig sonarServerConfig;

        DownloadIssuesTaskBuilder buildFrom(
                SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
                List<PsiFile> psiFiles) {
            downloadIssuesTask=null;
            processing = true;
            checkNotNull(enrichedSettings);
            if (processing) initSettings(enrichedSettings);
            if (processing) checkNotNullServerName();
            if (processing) initSonarServerConfig();
            if (processing) buildDownloadIssuesTask(enrichedSettings, psiFiles);
            return this;
        }

        private void checkNotNull(SonarQubeInspectionContext.EnrichedSettings enrichedSettings) {
            if (enrichedSettings.settings == null) {
                processing = false;
            }
        }

        private void initSettings(SonarQubeInspectionContext.EnrichedSettings enrichedSettings) {
            settings = enrichedSettings.settings.enrichWithProjectSettings(enrichedSettings.project);
        }

        private void checkNotNullServerName() {
            serverName = settings.getServerName();
            if (serverName == null) {
                processing = false;
            }
        }

        private void initSonarServerConfig() {
            final Optional<SonarServerConfig> maybeSonarServerConfig = SonarServers.get(serverName);
            if (!maybeSonarServerConfig.isPresent()) {
                processing = false;
            } else {
                sonarServerConfig = maybeSonarServerConfig.get();
            }
        }

        private void buildDownloadIssuesTask(SonarQubeInspectionContext.EnrichedSettings enrichedSettings, List<PsiFile> psiFiles) {
            final Set<String> resourceKeys = settings.getResources().stream().map(Resource::getKey).collect(Collectors.toSet());
            downloadIssuesTask =
                    new DownloadIssuesTask(
                            enrichedSettings,
                            sonarServerConfig,
                            resourceKeys,psiFiles
                    );
        }

        Optional<DownloadIssuesTask> maybeGetDownloadIssuesTask() {
            return Optional.ofNullable(downloadIssuesTask);
        }
    }
}
