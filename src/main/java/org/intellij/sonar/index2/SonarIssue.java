package org.intellij.sonar.index2;

import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Transient;

public class SonarIssue implements Comparable {
  private Integer line;
  private String message;
  private String severity;
  private Boolean isNew;

  public SonarIssue() {
  }

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

  public Integer getLine() {
    return line;
  }

  public void setLine(Integer line) {
    this.line = line;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public Boolean getIsNew() {
    return isNew;
  }

  public void setIsNew(Boolean isNew) {
    this.isNew = isNew;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarIssue that = (SonarIssue) o;

    if (isNew != null ? !isNew.equals(that.isNew) : that.isNew != null)
      return false;
    if (line != null ? !line.equals(that.line) : that.line != null)
      return false;
    if (message != null ? !message.equals(that.message) : that.message != null)
      return false;
    if (severity != null ? !severity.equals(that.severity) : that.severity != null)
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

  @Override
  public int compareTo(Object that) {
    return Objects.equal(this, that) ? 0 : -1 ;
  }
}
