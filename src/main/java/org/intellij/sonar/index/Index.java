package org.intellij.sonar.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;

public class Index {

  private final ImmutableMap<Key, ImmutableSet<Entry>> value;

  public Index(ImmutableMap<Key, ImmutableSet<Entry>> value) {
    this.value = value;
  }

  public ImmutableMap<Key, ImmutableSet<Entry>> getValue() {
    return value;
  }

  public static class Key {
    private final String fullFilePath;
    private final boolean isNew;
    private final String ruleKey;

    public Key(String fullFilePath, boolean isNew, String ruleKey) {
      this.fullFilePath = fullFilePath;
      this.isNew = isNew;
      this.ruleKey = ruleKey;
    }

    public String getFullFilePath() {
      return fullFilePath;
    }

    public boolean isNew() {
      return isNew;
    }

    public String getRuleKey() {
      return ruleKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Key key = (Key) o;

      if (isNew != key.isNew) return false;
      if (fullFilePath != null ? !fullFilePath.equals(key.fullFilePath) : key.fullFilePath != null)
        return false;
      if (ruleKey != null ? !ruleKey.equals(key.ruleKey) : key.ruleKey != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = fullFilePath != null ? fullFilePath.hashCode() : 0;
      result = 31 * result + (isNew ? 1 : 0);
      result = 31 * result + (ruleKey != null ? ruleKey.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Key{" +
          "fullFilePath='" + fullFilePath + '\'' +
          ", isNew=" + isNew +
          ", ruleKey='" + ruleKey + '\'' +
          '}';
    }
  }

  public static class Entry {
    private final String key;
    private final String componentKey;
    private final String ruleKey;
    private final String severity;
    private final String message;
    private final Integer line;
    private final String status;
    private final DateTime creationDate;
    private final DateTime updateDate;

    public Entry(String key, String componentKey, String ruleKey, String severity, String message, Integer line, String status, DateTime creationDate, DateTime updateDate) {
      this.key = key;
      this.componentKey = componentKey;
      this.ruleKey = ruleKey;
      this.severity = severity;
      this.message = message;
      this.line = line;
      this.status = status;
      this.creationDate = creationDate;
      this.updateDate = updateDate;
    }

    public String getKey() {
      return key;
    }

    public String getComponentKey() {
      return componentKey;
    }

    public String getRuleKey() {
      return ruleKey;
    }

    public String getSeverity() {
      return severity;
    }

    public String getMessage() {
      return message;
    }

    public Integer getLine() {
      return line;
    }

    public String getStatus() {
      return status;
    }

    public DateTime getCreationDate() {
      return creationDate;
    }

    public DateTime getUpdateDate() {
      return updateDate;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Entry entry = (Entry) o;

      if (componentKey != null ? !componentKey.equals(entry.componentKey) : entry.componentKey != null)
        return false;
      if (creationDate != null ? !creationDate.equals(entry.creationDate) : entry.creationDate != null)
        return false;
      if (key != null ? !key.equals(entry.key) : entry.key != null)
        return false;
      if (line != null ? !line.equals(entry.line) : entry.line != null)
        return false;
      if (message != null ? !message.equals(entry.message) : entry.message != null)
        return false;
      if (ruleKey != null ? !ruleKey.equals(entry.ruleKey) : entry.ruleKey != null)
        return false;
      if (severity != null ? !severity.equals(entry.severity) : entry.severity != null)
        return false;
      if (status != null ? !status.equals(entry.status) : entry.status != null)
        return false;
      if (updateDate != null ? !updateDate.equals(entry.updateDate) : entry.updateDate != null)
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
      result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
      result = 31 * result + (updateDate != null ? updateDate.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Entry{" +
          "key='" + key + '\'' +
          ", componentKey='" + componentKey + '\'' +
          ", ruleKey='" + ruleKey + '\'' +
          ", severity='" + severity + '\'' +
          ", message='" + message + '\'' +
          ", line=" + line +
          ", status='" + status + '\'' +
          ", creationDate=" + creationDate +
          ", updateDate=" + updateDate +
          '}';
    }
  }

}
