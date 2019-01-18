package org.intellij.sonar.index;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.persistence.Resource;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.sonarreport.data.Issue;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.intellij.sonar.util.SonarComponentToFileMatcher;
import org.sonarqube.ws.Issues;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IssuesByFileIndexer {

  private final List<PsiFile> files;
  private List<Issue> issues;
  private SonarConsole sonarConsole;

  public IssuesByFileIndexer(List<PsiFile> files) {
    this.files = files;
  }

  public void setIssues(List<Issue> issues) {
    this.issues = issues;
  }

  public IssuesByFileIndexer withSonarReportIssues(List<Issue> issues) {
    setIssues(ImmutableList.copyOf(issues));
    return this;
  }

  public IssuesByFileIndexer withSonarConsole(SonarConsole sonarConsole) {
    this.sonarConsole = sonarConsole;
    return this;
  }

  public IssuesByFileIndexer withSonarServerIssues(ImmutableList<Issues.Issue> issues) {
    setIssues(
            issues.stream().map(issue -> new Issue(
                    issue.getKey(),
                    issue.getComponent(),
                    issue.getLine(),
                    issue.getMessage(),
                    issue.getSeverity().name(),
                    issue.getRule(),
                    issue.getStatus(),
                    false // issues from sonar server cannot be new
            )).collect(Collectors.toList())
    );
    return this;
  }

  public Map<String,Set<SonarIssue>> create() {
    return new SonarIssuesIndexBuilder()
            .buildIndex()
            .getIndex();
  }



  private class SonarIssuesIndexBuilder {
    private Map<String, Set<SonarIssue>> index;
    private int filesCount;
    private AtomicInteger fileIndex;
    private ProgressIndicator indicator;

    public SonarIssuesIndexBuilder buildIndex() {
      filesCount = files.size();
      fileIndex = new AtomicInteger(0);

      indicator = ProgressManager.getInstance().getProgressIndicator();
      final int availableProcessors = Runtime.getRuntime().availableProcessors();

      info(
              String.format(
                      "Start processing %d files and %d issues with %d threads",
                      filesCount,
                      issues.size(),
                      availableProcessors
              )
      );

      final ExecutorService executorService = Executors.newFixedThreadPool(availableProcessors);
      final Iterable<List<PsiFile>> filePartitions = Iterables.partition(files,availableProcessors);

      index = Maps.newConcurrentMap();
      for (final List<PsiFile> partition : filePartitions) {
        executorService.execute(() -> createIndexFor(partition));
      }
      executorService.shutdown();
      try {
        executorService.awaitTermination(Long.MAX_VALUE,TimeUnit.NANOSECONDS);
      } catch (InterruptedException e) {
        sonarConsole.error(Throwables.getStackTraceAsString(e));
      }
      return this;
    }

    private void createIndexFor(List<PsiFile> psiFiles) {
      for (PsiFile psiFile : psiFiles) {
        if (indicator.isCanceled()) break;

        final int currentFileIndex = fileIndex.incrementAndGet();
        ProgressIndicatorUtil.setFraction(indicator,1.0 * currentFileIndex / filesCount);
        ProgressIndicatorUtil.setText2(indicator,psiFile.getName());
        final String filesProgressMessage = String.format("%d / %d files processed",currentFileIndex, filesCount);
        ProgressIndicatorUtil.setText(indicator,filesProgressMessage);

        if (filesCount % currentFileIndex == 20) {
          info(filesProgressMessage);
        }

        String fullFilePath = psiFile.getVirtualFile().getPath();

        final Settings settings = Settings.getSettingsFor(psiFile);
        if (settings == null) continue;

        final Collection<Resource> resources = settings.getResources();

        final Set<SonarIssue> sonarIssues = buildSonarIssues(fullFilePath, resources);
        if (!sonarIssues.isEmpty())
          index.put(fullFilePath,sonarIssues);
      }
    }

    private Set<SonarIssue> buildSonarIssues(
            String fullFilePath,
            Collection<Resource> resources) {
      ImmutableSet.Builder<SonarIssue> entriesBuilder = ImmutableSet.builder();
      if (resources == null || resources.isEmpty()) {
        matchFileByResource(fullFilePath,entriesBuilder,null);
      } else {
        for (Resource resource : resources) {
          matchFileByResource(fullFilePath,entriesBuilder,resource.getKey());
        }
      }
      return entriesBuilder.build();
    }

    private void info(String msg) {
      if (sonarConsole != null) {
        sonarConsole.info(msg);
      }
    }

    private void matchFileByResource(
            String fullFilePath,
            ImmutableSet.Builder<SonarIssue> entriesBuilder,
            String resourceKey
    ) {
      for (Issue issue : issues) {
        String component = issue.getComponent();
        if (new SonarComponentToFileMatcher().match(component,resourceKey,fullFilePath)) {
          entriesBuilder.add(
                  new SonarIssue(
                          issue.getKey(),
                          issue.getRule(),
                          issue.getLine(),
                          issue.getMessage(),
                          issue.getSeverity(),
                          issue.getIsNew()
                  )
          );
        }
      }
    }

    public Map<String, Set<SonarIssue>> getIndex() {
      return index;
    }
  }
}
