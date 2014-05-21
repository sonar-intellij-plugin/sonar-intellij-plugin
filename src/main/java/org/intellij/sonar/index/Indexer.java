package org.intellij.sonar.index;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.sonarreport.data.Issue;
import org.intellij.sonar.util.SonarComponentToFileMatcher;
import org.sonar.wsclient.services.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer {
  private final static Logger LOG = Logger.getInstance(Indexer.class);

  private ImmutableList<Issue> issues;
  private Map<String, Issue> issuesByKey = new ConcurrentHashMap<String, Issue>();
  private final ImmutableList<VirtualFile> files;
  private final ImmutableList<Resource> sonarResources;

  public Indexer(ImmutableList<VirtualFile> virtualFiles, ImmutableList<Resource> sonarResources) {
    this.files = virtualFiles;
    this.sonarResources = sonarResources;
  }

  public void setIssues(ImmutableList<Issue> issues) {
    this.issues = issues;
    for (Issue issue: issues) {
      issuesByKey.put(issue.getKey(), issue);
    }
  }

  public Indexer withSonarReportIssues(List<Issue> issues) {
    setIssues(ImmutableList.copyOf(issues));
    return this;
  }

  public Indexer withSonarServerIssues(ImmutableList<org.sonar.wsclient.issue.Issue> issues) {
    setIssues(FluentIterable.from(issues)
        .transform(new Function<org.sonar.wsclient.issue.Issue, Issue>() {
          @Override
          public Issue apply(org.sonar.wsclient.issue.Issue issue) {
            return new Issue(
                issue.key(),
                issue.componentKey(),
                issue.line(),
                issue.message(),
                issue.severity(),
                issue.ruleKey(),
                issue.status(),
                false // issues from sonar server cannot be new
            );
          }
        }).toList());
    return this;
  }

  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> create() {

    final ConcurrentHashMap<IssuesIndexKey, Set<IssuesIndexEntry>> index = new ConcurrentHashMap<IssuesIndexKey, Set<IssuesIndexEntry>>();
    for (Resource sonarResource : sonarResources) {
      String resourceKey = sonarResource.getKey();
      for (VirtualFile projectFile : files) {
        String fullFilePath = projectFile.getPath();
        ImmutableList.Builder<IssuesIndexEntry> entriesBuilder = ImmutableList.builder();
        for (Issue issue : issues) {
          String component = issue.getComponent();
          if (SonarComponentToFileMatcher.match(component, resourceKey, fullFilePath)) {
            entriesBuilder.add(new IssuesIndexEntry(
                issue.getKey(),
                issue.getComponent(),
                issue.getRule(),
                issue.getSeverity(),
                issue.getMessage(),
                issue.getLine(),
                issue.getStatus()
            ));
          }
        }
        final ImmutableList<IssuesIndexEntry> entries = entriesBuilder.build();
        for (IssuesIndexEntry entry: entries) {
          final Issue issue = issuesByKey.get(entry.getKey());
          final IssuesIndexKey key = new IssuesIndexKey(fullFilePath, issue.getIsNew(), entry.getRuleKey());
          index.putIfAbsent(key, new HashSet<IssuesIndexEntry>());
          final ImmutableSet<IssuesIndexEntry> newEntries = ImmutableSet.<IssuesIndexEntry>builder().addAll(index.get(key)).add(entry).build();
          index.replace(key, new HashSet<IssuesIndexEntry>(newEntries));
        }
      }
    }

    return index;
  }
}
