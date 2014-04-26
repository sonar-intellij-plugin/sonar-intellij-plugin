package org.intellij.sonar.index;

public class IssuesIndexKey {
  private String fullFilePath;
  private Boolean isNew;
  private String ruleKey;

  public IssuesIndexKey() {
  }

  public IssuesIndexKey(String fullFilePath, Boolean isNew, String ruleKey) {
    this.fullFilePath = fullFilePath;
    this.isNew = isNew;
    this.ruleKey = ruleKey;
  }

  public String getFullFilePath() {
    return fullFilePath;
  }

  public void setFullFilePath(String fullFilePath) {
    this.fullFilePath = fullFilePath;
  }

  public Boolean getIsNew() {
    return isNew;
  }

  public void setIsNew(Boolean isNew) {
    this.isNew = isNew;
  }

  public String getRuleKey() {
    return ruleKey;
  }

  public void setRuleKey(String ruleKey) {
    this.ruleKey = ruleKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IssuesIndexKey that = (IssuesIndexKey) o;

    if (fullFilePath != null ? !fullFilePath.equals(that.fullFilePath) : that.fullFilePath != null)
      return false;
    if (isNew != null ? !isNew.equals(that.isNew) : that.isNew != null)
      return false;
    if (ruleKey != null ? !ruleKey.equals(that.ruleKey) : that.ruleKey != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fullFilePath != null ? fullFilePath.hashCode() : 0;
    result = 31 * result + (isNew != null ? isNew.hashCode() : 0);
    result = 31 * result + (ruleKey != null ? ruleKey.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IssuesIndexKey{" +
        "fullFilePath='" + fullFilePath + '\'' +
        ", isNew=" + isNew +
        ", ruleKey='" + ruleKey + '\'' +
        '}';
  }
}
