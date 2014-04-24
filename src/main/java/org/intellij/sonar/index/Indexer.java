package org.intellij.sonar.index;

import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.util.SonarComponentToFileMatcher;
import org.intellij.sonar.sonarreport.Issue;
import org.joda.time.DateTime;
import org.sonar.wsclient.services.Resource;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.intellij.sonar.index.Index.Entry;
import static org.intellij.sonar.index.Index.Key;

public class Indexer {
  private final static Logger LOG = Logger.getInstance(Indexer.class);

  private ImmutableList<Issue> issues;
  private final ImmutableList<VirtualFile> files;
  private final ImmutableList<Resource> sonarResources;

  public Indexer(ImmutableList<VirtualFile> virtualFiles, ImmutableList<Resource> sonarResources) {
    this.files = virtualFiles;
    this.sonarResources = sonarResources;
  }

  public Indexer withSonarReportIssues(ImmutableList<Issue> issues) {
    this.issues = issues;
    return this;
  }

  public Indexer withSonarServerIssues(ImmutableList<org.sonar.wsclient.issue.Issue> issues) {
    this.issues = FluentIterable.from(issues)
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
                false,
                null != issue.creationDate() ? new DateTime(issue.creationDate()) : null,
                null != issue.updateDate() ? new DateTime(issue.updateDate()) : null
            );
          }
        }).toList();
    return this;
  }

  public ImmutableMap<Key, ImmutableSet<Entry>> create() {

    final ConcurrentHashMap<Key, ImmutableSet<Entry>> index = new ConcurrentHashMap<Key, ImmutableSet<Entry>>();
    for (Resource sonarResource : sonarResources) {
      String resourceKey = sonarResource.getKey();
      for (VirtualFile projectFile : files) {
        String fullFilePath = projectFile.getPath();
        ImmutableList.Builder<Entry> entriesBuilder = ImmutableList.builder();
        for (Issue issue : issues) {
          String component = issue.getComponent();
          if (SonarComponentToFileMatcher.match(component, resourceKey, fullFilePath)) {
            entriesBuilder.add(new Entry(
                issue.getKey(),
                issue.getComponent(),
                issue.getRule(),
                issue.getSeverity(),
                issue.getMessage(),
                issue.getLine(),
                issue.getStatus(),
                issue.getCreationDate(),
                issue.getUpdateDate()
            ));
          }
        }
        final ImmutableList<Entry> entries = entriesBuilder.build();
        for (Entry entry: entries) {
          final Key key = new Key(fullFilePath, false, entry.getRuleKey());
          index.putIfAbsent(key, ImmutableSet.<Entry>of());
          index.put(key, ImmutableSet.<Entry>builder().addAll(index.get(key)).add(entry).build());
        }
      }
    }

    return ImmutableMap.copyOf(index);
  }
}
