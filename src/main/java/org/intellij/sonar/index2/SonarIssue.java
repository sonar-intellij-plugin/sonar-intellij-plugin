package org.intellij.sonar.index2;

import com.intellij.util.xmlb.annotations.Transient;

public class SonarIssue {
  public Integer line;
  public String message;
  public String severity;
  public Boolean isNew;

  public SonarIssue(Integer line, String message, String severity, Boolean isNew) {
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

    SonarIssue sonarIssue = (SonarIssue) o;

    if (isNew != null ? !isNew.equals(sonarIssue.isNew) : sonarIssue.isNew != null)
      return false;
    if (line != null ? !line.equals(sonarIssue.line) : sonarIssue.line != null)
      return false;
    if (message != null ? !message.equals(sonarIssue.message) : sonarIssue.message != null)
      return false;
    if (severity != null ? !severity.equals(sonarIssue.severity) : sonarIssue.severity != null)
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
