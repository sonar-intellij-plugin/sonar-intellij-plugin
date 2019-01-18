package org.intellij.sonar.sonarreport.data;

import com.google.common.base.Objects;

public class SonarRule {

  private String key;
  private String rule;
  private String repository;
  private String name;

  public SonarRule(String key, String rule, String repository, String name) {
    this.key = key;
    this.rule = rule;
    this.repository = repository;
    this.name = name;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public void setRepository(String repository) {
    this.repository = repository;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarRule rule1 = (SonarRule) o;
    return Objects.equal(key, rule1.key) &&
            Objects.equal(rule, rule1.rule) &&
            Objects.equal(repository, rule1.repository) &&
            Objects.equal(name, rule1.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, rule, repository, name);
  }

  @Override
  public String toString() {
    return "Rule{"+
      "key='"+key+'\''+
      ", rule='"+rule+'\''+
      ", repository='"+repository+'\''+
      ", name='"+name+'\''+
      '}';
  }
}
