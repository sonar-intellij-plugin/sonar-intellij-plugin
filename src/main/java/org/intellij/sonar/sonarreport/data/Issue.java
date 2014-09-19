package org.intellij.sonar.sonarreport.data;

public class Issue {

  private String key;
  private String component;
  private Integer line;
  private String message;
  private String severity;
  private String rule;
  private String status;
  private Boolean isNew;

  public Issue(String key, String component, Integer line, String message, String severity, String rule, String status, Boolean isNew) {
    this.key = key;
    this.component = component;
    this.line = line;
    this.message = message;
    this.severity = severity;
    this.rule = rule;
    this.status = status;
    this.isNew = isNew;
  }

  public String getKey() {
    return key;
  }

  public String getComponent() {
    return component;
  }

  public Integer getLine() {
    return line;
  }

  public String getMessage() {
    return message;
  }

  public String getSeverity() {
    return severity;
  }

  public String getRule() {
    return rule;
  }

  public String getStatus() {
    return status;
  }

  public Boolean getIsNew() {
    return isNew;
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Issue issue = (Issue) o;

    if (component != null ? !component.equals(issue.component) : issue.component != null)
      return false;
    if (isNew != null ? !isNew.equals(issue.isNew) : issue.isNew != null)
      return false;
    if (key != null ? !key.equals(issue.key) : issue.key != null)
      return false;
    if (line != null ? !line.equals(issue.line) : issue.line != null)
      return false;
    if (message != null ? !message.equals(issue.message) : issue.message != null)
      return false;
    if (rule != null ? !rule.equals(issue.rule) : issue.rule != null)
      return false;
    if (severity != null ? !severity.equals(issue.severity) : issue.severity != null)
      return false;
    if (status != null ? !status.equals(issue.status) : issue.status != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (component != null ? component.hashCode() : 0);
    result = 31 * result + (line != null ? line.hashCode() : 0);
    result = 31 * result + (message != null ? message.hashCode() : 0);
    result = 31 * result + (severity != null ? severity.hashCode() : 0);
    result = 31 * result + (rule != null ? rule.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (isNew != null ? isNew.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Issue{" +
        "key='" + key + '\'' +
        ", component='" + component + '\'' +
        ", line=" + line +
        ", message='" + message + '\'' +
        ", severity='" + severity + '\'' +
        ", rule='" + rule + '\'' +
        ", status='" + status + '\'' +
        ", isNew=" + isNew +
        '}';
  }
}
