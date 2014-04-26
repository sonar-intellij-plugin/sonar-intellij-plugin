package org.intellij.sonar.index;

import com.google.common.base.Objects;

public class IssuesIndexEntry implements Comparable{
  private String key;
  private String componentKey;
  private String ruleKey;
  private String severity;
  private String message;
  private Integer line;
  private String status;

  public IssuesIndexEntry() {
  }

  public IssuesIndexEntry(String key, String componentKey, String ruleKey, String severity, String message, Integer line, String status) {
    this.key = key;
    this.componentKey = componentKey;
    this.ruleKey = ruleKey;
    this.severity = severity;
    this.message = message;
    this.line = line;
    this.status = status;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getComponentKey() {
    return componentKey;
  }

  public void setComponentKey(String componentKey) {
    this.componentKey = componentKey;
  }

  public String getRuleKey() {
    return ruleKey;
  }

  public void setRuleKey(String ruleKey) {
    this.ruleKey = ruleKey;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getLine() {
    return line;
  }

  public void setLine(Integer line) {
    this.line = line;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IssuesIndexEntry that = (IssuesIndexEntry) o;

    if (componentKey != null ? !componentKey.equals(that.componentKey) : that.componentKey != null)
      return false;
    if (key != null ? !key.equals(that.key) : that.key != null)
      return false;
    if (line != null ? !line.equals(that.line) : that.line != null)
      return false;
    if (message != null ? !message.equals(that.message) : that.message != null)
      return false;
    if (ruleKey != null ? !ruleKey.equals(that.ruleKey) : that.ruleKey != null)
      return false;
    if (severity != null ? !severity.equals(that.severity) : that.severity != null)
      return false;
    if (status != null ? !status.equals(that.status) : that.status != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (componentKey != null ? componentKey.hashCode() : 0);
    result = 31 * result + (ruleKey != null ? ruleKey.hashCode() : 0);
    result = 31 * result + (severity != null ? severity.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + (line != null ? line.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IssuesIndexEntry{" +
        "key='" + key + '\'' +
        ", componentKey='" + componentKey + '\'' +
        ", ruleKey='" + ruleKey + '\'' +
        ", severity='" + severity + '\'' +
        ", message='" + message + '\'' +
        ", line=" + line +
        ", status='" + status + '\'' +
        '}';
  }

  // we care only about equal case
  // see also http://youtrack.jetbrains.com/issue/IDEA-124480
  @Override
  public int compareTo(Object that) {
    return Objects.equal(this, that) ? 0 : -1 ;
  }
}
