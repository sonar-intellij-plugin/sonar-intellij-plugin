package org.intellij.sonar.index2;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.util.xmlb.annotations.Transient;
import org.intellij.sonar.SonarSeverity;

import java.util.Map;
import java.util.Set;

public class IssuesByFileIndex {
  public static final Map<String, Set<MyIssue>> index = Maps.newLinkedHashMap();
  static {
    Set<MyIssue> issues = Sets.newLinkedHashSet();
    issues.add(new MyIssue(11, "blocker issue", SonarSeverity.BLOCKER.toString(), true));
    issues.add(new MyIssue(12, "info issue", SonarSeverity.INFO.toString(), false));
    issues.add(new MyIssue(13, "major issue", SonarSeverity.MAJOR.toString(), true));
    issues.add(new MyIssue(10, "BLOCKER issue", SonarSeverity.MAJOR.toString(), false));
    issues.add(new MyIssue(10, "MAJOR issue", SonarSeverity.MAJOR.toString(), true));
    issues.add(new MyIssue(10, "INFO issue", SonarSeverity.MAJOR.toString(), false));
    issues.add(new MyIssue(null, "file level issue", SonarSeverity.MAJOR.toString(), true));
    index.put(
        "/Users/omayevskiy/workspace/SonarSource-sonar-examples-4a4a681/projects/languages/java/maven/java-maven-simple/src/main/java/example/One.java",
        issues
        );
  }

  public static class MyIssue {
    public Integer line;
    public String message;
    public String severity;
    public Boolean isNew;

    public MyIssue(Integer line, String message, String severity, Boolean isNew) {
      this.severity = severity;
      this.message = message;
      this.line = line;
      this.isNew = isNew;
    }

    @Transient
    public String formattedMessage() {
      return String.format("[%s] %s", this.severity, this.message);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyIssue myIssue = (MyIssue) o;

      if (isNew != null ? !isNew.equals(myIssue.isNew) : myIssue.isNew != null)
        return false;
      if (line != null ? !line.equals(myIssue.line) : myIssue.line != null)
        return false;
      if (message != null ? !message.equals(myIssue.message) : myIssue.message != null)
        return false;
      if (severity != null ? !severity.equals(myIssue.severity) : myIssue.severity != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = line != null ? line.hashCode() : 0;
      result = 31 * result + (message != null ? message.hashCode() : 0);
      result = 31 * result + (severity != null ? severity.hashCode() : 0);
      result = 31 * result + (isNew != null ? isNew.hashCode() : 0);
      return result;
    }
  }

  public static Set<MyIssue> getIssuesForFile(String fullPath) {
    Set<MyIssue> issues = index.get(fullPath);
    if (issues == null) {
      index.put(fullPath, Sets.<MyIssue>newLinkedHashSet());
      return index.get(fullPath);
    }
    return issues;
  }
}
