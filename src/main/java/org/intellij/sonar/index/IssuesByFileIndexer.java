package org.intellij.sonar.index;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.sonarreport.data.Issue;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.intellij.sonar.util.SettingsUtil;
import org.intellij.sonar.util.SonarComponentToFileMatcher;
import org.sonar.wsclient.services.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IssuesByFileIndexer {
    private final ImmutableList<PsiFile> files;
    private ImmutableList<Issue> issues;
    private SonarConsole sonarConsole;

    public IssuesByFileIndexer(ImmutableList<PsiFile> files) {
        this.files = files;
    }

    public void setIssues(ImmutableList<Issue> issues) {
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
        final int filesCount = files.size();
        int fileIndex = 0;
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        info(String.format("Start processing %d files and %d issues", filesCount, issues.size()));
        for (PsiFile psiFile : files) {

            if (indicator.isCanceled()) break;
            fileIndex++;
            ProgressIndicatorUtil.setFraction(indicator, 1.0 * fileIndex / filesCount);
            ProgressIndicatorUtil.setText2(indicator, psiFile.getName());
            final String filesProgressMessage = String.format("%d / %d files processed", fileIndex, filesCount);
            ProgressIndicatorUtil.setText(indicator, filesProgressMessage);
            if (filesCount % fileIndex == 20) {
                info(filesProgressMessage);
            }

            String fullFilePath = psiFile.getVirtualFile().getPath();
            ImmutableSet.Builder<SonarIssue> entriesBuilder = ImmutableSet.builder();
            final Settings settings = SettingsUtil.getSettingsFor(psiFile);
            if (settings == null) continue;
            final Collection<Resource> resources = settings.getResources();
            if (resources == null || resources.isEmpty()) {
                matchFileByResource(fullFilePath, entriesBuilder, new Resource());
            } else {
                for (Resource resource : resources) {
                    matchFileByResource(fullFilePath, entriesBuilder, resource);
                }
            }

            final ImmutableSet<SonarIssue> sonarIssues = entriesBuilder.build();
            if (!sonarIssues.isEmpty())
                index.put(fullFilePath, sonarIssues);
        }
        return index;
    }

    private void info(String msg) {
        if (sonarConsole != null) {
            sonarConsole.info(msg);
        }
    }

    private void matchFileByResource(String fullFilePath, ImmutableSet.Builder<SonarIssue> entriesBuilder, Resource resource) {
        final String resourceKey = resource.getKey();
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
    }
}
