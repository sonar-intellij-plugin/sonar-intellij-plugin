package org.intellij.sonar.index;

import com.google.common.base.Objects;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

public class SonarIssue implements Comparable {

  private String key;
  private String ruleKey;
  private Integer line;
  private String message;
  private String severity;
  private Boolean isNew;

  public SonarIssue() {
  }

  public SonarIssue(String key,String ruleKey,Integer line,String message,String severity,Boolean isNew) {
    this.key = key;
    this.ruleKey = ruleKey;
    this.severity = severity;
    this.message = message;
    this.line = line;
    this.isNew = isNew;
  }

  @Transient
  public String formattedMessage() {
    return String.format("[%s] %s",this.severity,this.message);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getRuleKey() {
    return ruleKey;
  }

  public void setRuleKey(String ruleKey) {
    this.ruleKey = ruleKey;
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
    return Objects.equal(line, that.line) &&
            Objects.equal(message, that.message) &&
            Objects.equal(severity, that.severity) &&
            Objects.equal(isNew, that.isNew);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(line, message, severity, isNew);
  }

  @Override
  public int compareTo(@NotNull Object that) {
    return Objects.equal(this,that)
      ? 0
      : -1;
  }
}
