package org.intellij.sonar.index;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.fest.assertions.MapAssert;
import org.intellij.sonar.persistence.IndexComponent;
import org.jdom.Element;
import org.junit.Test;
import org.sonar.wsclient.issue.Issue;
import org.sonar.wsclient.issue.IssueComment;
import org.sonar.wsclient.issue.WorkDayDuration;
import org.sonar.wsclient.services.Resource;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.MapAssert.entry;

public class IndexerTests {

  private static final ImmutableList<VirtualFile> PROJECT_FILES = FluentIterable.from(ImmutableList.of(
          new MockVirtualFile("/my/dir/project/src/main/java/foo/bar/Bar.java"),
          new MockVirtualFile("/my/dir/project/src/main/java/foo/Foo.java"),
          new MockVirtualFile("/my/dir/project/src/main/java/Main.java"))
  ).transform(new Function<MockVirtualFile, VirtualFile>() {
    @Override
    public VirtualFile apply(MockVirtualFile mockVirtualFile) {
      return mockVirtualFile;
    }
  }).toList();

  private static final ImmutableList<Resource> SONAR_RESOURCES;

  static {
    final Resource resource = new Resource();
    resource.setKey("sonar:project");
    SONAR_RESOURCES = ImmutableList.of(resource);
  }

  private static final MapAssert.Entry[] EXPECTED_ENTRIES = new MapAssert.Entry[]{entry(
      new IssuesIndexKey("MOCK_ROOT://my/dir/project/src/main/java/foo/bar/Bar.java", false, "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck"),
      ImmutableSet.of(
          new IssuesIndexEntry(null, "sonar:project:foo.bar.Bar", "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck", null, "Simplify Boolean Expression", 11, null ),
          new IssuesIndexEntry(null, "sonar:project:foo.bar.Bar", "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck", null, "Simplify Boolean Expression", 12, null)
      )
  ),
      entry(
          new IssuesIndexKey("MOCK_ROOT://my/dir/project/src/main/java/foo/Foo.java", false, "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck"),
          ImmutableSet.of(
              new IssuesIndexEntry(null, "sonar:project:foo.Foo", "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck", null, "Simplify Boolean Expression", 3, null)
          )
      ),
      entry(
          new IssuesIndexKey("MOCK_ROOT://my/dir/project/src/main/java/Main.java", false, "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck"),
          ImmutableSet.of(
              new IssuesIndexEntry(null, "sonar:project:[default].Main", "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck", null, "Simplify Boolean Expression", 7, null)
          )
      )};

  @Test
  public void createIndexFromSonarServerIssuesShouldWork() {

    ImmutableList<Issue> sonarIssues = FluentIterable.from(ImmutableList.of(
        new MockIssue(
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            "sonar:project:foo.bar.Bar",
            11,
            "Simplify Boolean Expression"),
        new MockIssue(
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            "sonar:project:foo.bar.Bar",
            12,
            "Simplify Boolean Expression"),
        new MockIssue(
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            "sonar:project:[default].Main",
            7,
            "Simplify Boolean Expression"),
        new MockIssue(
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            "sonar:project:foo.Foo",
            3,
            "Simplify Boolean Expression")
    )).transform(new Function<MockIssue, Issue>() {
      @Override
      public Issue apply(MockIssue mockIssue) {
        return mockIssue;
      }
    }).toList();

    Indexer indexer = new Indexer(PROJECT_FILES, SONAR_RESOURCES).withSonarServerIssues(sonarIssues);

    final Map<IssuesIndexKey, Set<IssuesIndexEntry>> index = indexer.create();

    assertThat(index)
        .includes(
            EXPECTED_ENTRIES
        );

    final IndexComponent indexComponent = new IndexComponent();
    indexComponent.setIssuesIndex(index);
    final Element element = XmlSerializer.serialize(indexComponent);
    final IndexComponent deserialize = XmlSerializer.deserialize(element, IndexComponent.class);
  }

  @Test
  public void createIndexFromSonarReportIssuesShouldWork() {

    ImmutableList<org.intellij.sonar.sonarreport.Issue> issues = ImmutableList.of(
        new org.intellij.sonar.sonarreport.Issue(
            null,
            "sonar:project:foo.bar.Bar",
            11,
            "Simplify Boolean Expression",
            null,
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            null,
            true,
            null,
            null
        ),
        new org.intellij.sonar.sonarreport.Issue(
            null,
            "sonar:project:foo.bar.Bar",
            12,
            "Simplify Boolean Expression",
            null,
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            null,
            false,
            null,
            null
        ),
        new org.intellij.sonar.sonarreport.Issue(
            null,
            "sonar:project:[default].Main",
            7,
            "Simplify Boolean Expression",
            null,
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            null,
            false,
            null,
            null
        ),
        new org.intellij.sonar.sonarreport.Issue(
            null,
            "sonar:project:foo.Foo",
            3,
            "Simplify Boolean Expression",
            null,
            "checkstyle:com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
            null,
            false,
            null,
            null
        )
    );

    Indexer indexer = new Indexer(PROJECT_FILES, SONAR_RESOURCES).withSonarReportIssues(issues);

    final Map<IssuesIndexKey, Set<IssuesIndexEntry>> index = indexer.create();

    assertThat(index)
        .includes(
            EXPECTED_ENTRIES
        );

    final IndexComponent indexComponent = new IndexComponent();
    indexComponent.setIssuesIndex(index);
    indexComponent.loadState(indexComponent);
    XmlSerializerUtil.getAccessors(IndexComponent.class);
  }

  private class MockIssue implements Issue {

    private final String ruleKey;
    private final String componentKey;
    private final Integer line;
    private final String message;

    private MockIssue(String ruleKey, String componentKey, Integer line, String message) {
      this.ruleKey = ruleKey;
      this.componentKey = componentKey;
      this.line = line;
      this.message = message;
    }

    @Override
    public String key() {
      return null;
    }

    @Override
    public String componentKey() {
      return componentKey;
    }

    @Override
    public String projectKey() {
      return null;
    }

    @Override
    public String ruleKey() {
      return ruleKey;
    }

    @Override
    public String severity() {
      return null;
    }

    @Override
    public String message() {
      return message;
    }

    @Override
    public Integer line() {
      return line;
    }

    @Override
    public Double effortToFix() {
      return null;
    }

    @Override
    public WorkDayDuration technicalDebt() {
      return null;
    }

    @Override
    public String status() {
      return null;
    }

    @Override
    public String resolution() {
      return null;
    }

    @Override
    public String reporter() {
      return null;
    }

    @Override
    public String assignee() {
      return null;
    }

    @Override
    public String author() {
      return null;
    }

    @Override
    public String actionPlan() {
      return null;
    }

    @Override
    public Date creationDate() {
      return null;
    }

    @Override
    public Date updateDate() {
      return null;
    }

    @Override
    public Date closeDate() {
      return null;
    }

    @Override
    public String attribute(String key) {
      return null;
    }

    @Override
    public Map<String, String> attributes() {
      return null;
    }

    @Override
    public List<IssueComment> comments() {
      return null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MockIssue mockIssue = (MockIssue) o;

      if (componentKey != null ? !componentKey.equals(mockIssue.componentKey) : mockIssue.componentKey != null)
        return false;
      if (line != null ? !line.equals(mockIssue.line) : mockIssue.line != null)
        return false;
      if (message != null ? !message.equals(mockIssue.message) : mockIssue.message != null)
        return false;
      if (ruleKey != null ? !ruleKey.equals(mockIssue.ruleKey) : mockIssue.ruleKey != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = ruleKey != null ? ruleKey.hashCode() : 0;
      result = 31 * result + (componentKey != null ? componentKey.hashCode() : 0);
      result = 31 * result + (line != null ? line.hashCode() : 0);
      result = 31 * result + (message != null ? message.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "MockIssue{" +
          "ruleKey='" + ruleKey + '\'' +
          ", componentKey='" + componentKey + '\'' +
          ", line=" + line +
          ", message='" + message + '\'' +
          '}';
    }
  }
}
