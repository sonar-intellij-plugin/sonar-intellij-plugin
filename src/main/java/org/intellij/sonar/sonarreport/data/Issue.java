package org.intellij.sonar.sonarreport.data;

import com.google.common.base.Objects;

public class Issue {

  private String key;
  private String component;
  private Integer line;
  private String message;
  private String severity;
  private String rule;
  private String status;
  private Boolean isNew;

  public Issue(
    String key,
    String component,
    Integer line,
    String message,
    String severity,
    String rule,
    String status,
    Boolean isNew
  ) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Issue issue = (Issue) o;
    return Objects.equal(key, issue.key) &&
            Objects.equal(component, issue.component) &&
            Objects.equal(line, issue.line) &&
            Objects.equal(message, issue.message) &&
            Objects.equal(severity, issue.severity) &&
            Objects.equal(rule, issue.rule) &&
            Objects.equal(status, issue.status) &&
            Objects.equal(isNew, issue.isNew);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, component, line, message, severity, rule, status, isNew);
  }

  @Override
  public String toString() {
    return "Issue{"+
      "key='"+key+'\''+
      ", component='"+component+'\''+
      ", line="+line+
      ", message='"+message+'\''+
      ", severity='"+severity+'\''+
      ", rule='"+rule+'\''+
      ", status='"+status+'\''+
      ", isNew="+isNew+
      '}';
  }
}
