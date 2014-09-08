package org.intellij.sonar.index2;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.sonarreport.data.Issue;
import org.intellij.sonar.util.SonarComponentToFileMatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IssuesByFileIndexer {
  private ImmutableList<Issue> issues;
  private Map<String, Issue> issuesByKey = new ConcurrentHashMap<String, Issue>();
  private final ImmutableList<PsiFile> files;
  private final String resourceKey;

  public IssuesByFileIndexer(ImmutableList<PsiFile> files, String resourceKey) {
    this.files = files;
    this.resourceKey = resourceKey;
  }

  public void setIssues(ImmutableList<Issue> issues) {
    this.issues = issues;
    for (Issue issue: issues) {
      issuesByKey.put(issue.getKey(), issue);
    }
  }

  public IssuesByFileIndexer withSonarReportIssues(List<Issue> issues) {
    setIssues(ImmutableList.copyOf(issues));
    return this;
  }

  public IssuesByFileIndexer withSonarServerIssues(ImmutableList<org.sonar.wsclient.issue.Issue> issues) {
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

  public Map<String, Set<SonarIssue>> create() {

    final Map<String, Set<SonarIssue>> index = Maps.newConcurrentMap();
      for (PsiFile psiFile : files) {
        String fullFilePath = psiFile.getVirtualFile().getPath();
        ImmutableSet.Builder<SonarIssue> entriesBuilder = ImmutableSet.builder();
        for (Issue issue : issues) {
          String component = issue.getComponent();
          if (SonarComponentToFileMatcher.match(component, resourceKey, fullFilePath)) {
            entriesBuilder.add(
                new SonarIssue(
                    issue.getKey(),
                    issue.getRule(),
                    issue.getLine(),
                    issue.getMessage(),
                    issue.getSeverity(),
                    issue.getIsNew()
                ));
          }
        }
        final ImmutableSet<SonarIssue> sonarIssues = entriesBuilder.build();
        if (!sonarIssues.isEmpty()) index.put(fullFilePath, sonarIssues);
      }

    return index;
  }
}
