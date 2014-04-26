package org.intellij.sonar.persistence;

public class SonarRuleBean {


  private String displayName;
  private String description;
  private String key;
  private String repository;
  private String severity;

  public SonarRuleBean() {
  }

  public SonarRuleBean(String displayName, String description, String key, String repository, String severity) {
    this.displayName = displayName;
    this.description = description;
    this.key = key;
    this.repository = repository;
    this.severity = severity;
  }

  /**
   * @return value like "java.lang.Error" should not be extended
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @return value like Make it better
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return value like squid:S1190
   */
  public String getKey() {
    return key;
  }

  /**
   *
   * @return value like squid
   */
  public String getRepository() {
    return repository;
  }

  /**
   *
   * @return value like MAJOR
   */
  public String getSeverity() {
    return severity;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarRuleBean that = (SonarRuleBean) o;

    if (description != null ? !description.equals(that.description) : that.description != null)
      return false;
    if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null)
      return false;
    if (key != null ? !key.equals(that.key) : that.key != null)
      return false;
    if (repository != null ? !repository.equals(that.repository) : that.repository != null)
      return false;
    if (severity != null ? !severity.equals(that.severity) : that.severity != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = displayName != null ? displayName.hashCode() : 0;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (repository != null ? repository.hashCode() : 0);
    result = 31 * result + (severity != null ? severity.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SonarRuleBean{" +
        "displayName='" + displayName + '\'' +
        ", description='" + description + '\'' +
        ", key='" + key + '\'' +
        ", repository='" + repository + '\'' +
        ", severity='" + severity + '\'' +
        '}';
  }
}
