package org.intellij.sonar.analysis;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
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
import org.intellij.sonar.util.SettingsUtil;
import org.sonarqube.ws.Issues.Issue;

import java.util.AbstractCollection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DownloadIssuesTask implements Runnable {

  private final SonarServerConfig sonarServerConfig;
  private final Set<String> resourceKeys;
  private final ImmutableList<PsiFile> psiFiles;
  private final Map<String,ImmutableList<Issue>> downloadedIssuesByResourceKey = Maps.newConcurrentMap();
  private final SonarQubeInspectionContext.EnrichedSettings enrichedSettings;
  private final SonarConsole sonarConsole;

  public DownloadIssuesTask(
    SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
    SonarServerConfig sonarServerConfig,
    Set<String> resourceKeys,
    ImmutableList<PsiFile> psiFiles
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
    final Settings settings = SettingsUtil.process(enrichedSettings.project,enrichedSettings.settings);
    final String serverName = settings.getServerName();
    if (serverName == null) return Optional.empty();
    final Optional<SonarServerConfig> c = SonarServers.get(serverName);
    if (!c.isPresent()) return Optional.empty();
    final Set<String> resourceKeys = settings.getResources().stream().map(Resource::getKey).collect(Collectors.toSet());
    return Optional.of(
      new DownloadIssuesTask(
        enrichedSettings,
        c.get(),
        resourceKeys,psiFiles
      )
    );
  }

  @Override
  public void run() {
    final SonarServer sonarServer = SonarServer.create(sonarServerConfig);
    final long startTime = System.currentTimeMillis();
    for (String resourceKey : resourceKeys) {
      final String downloadingIssuesMessage = String.format("Downloading issues for SonarQube resource %s",resourceKey);
      sonarConsole.info(downloadingIssuesMessage);
      final ImmutableList<Issue> issues = sonarServer.getAllIssuesFor(resourceKey);
      downloadedIssuesByResourceKey.put(resourceKey,issues);
    }
    onSuccess(startTime);
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
}
