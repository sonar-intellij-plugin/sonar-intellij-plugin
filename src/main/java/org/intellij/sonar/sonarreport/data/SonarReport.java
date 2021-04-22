package org.intellij.sonar.sonarreport.data;

import java.util.List;

import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.intellij.sonar.sonarreport.DateTimeTypeConverter;
import org.joda.time.DateTime;

public class SonarReport {

  private static final Gson GSON = new GsonBuilder()
          .registerTypeAdapter(DateTime.class,new DateTimeTypeConverter())
          .create();
  private String version;
  private List<Issue> issues;
  private List<Component> components;
  private List<SonarRule> rules;
  private List<User> users;

  public SonarReport(String version, List<Issue> issues, List<Component> components, List<SonarRule> sonarRules, List<User> users) {
    this.version = version;
    this.issues = issues;
    this.components = components;
    this.rules = sonarRules;
    this.users = users;
  }

  public String getVersion() {
    return version;
  }

  public List<Issue> getIssues() {
    return issues;
  }

  public List<Component> getComponents() {
    return components;
  }

  public List<SonarRule> getRules() {
    return rules;
  }

  public List<User> getUsers() {
    return users;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarReport that = (SonarReport) o;
    return Objects.equal(version, that.version) &&
            Objects.equal(issues, that.issues) &&
            Objects.equal(components, that.components) &&
            Objects.equal(rules, that.rules) &&
            Objects.equal(users, that.users);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(version, issues, components, rules, users);
  }

  @Override
  public String toString() {
    return "SonarReport{"+
      "version='"+version+'\''+
      ", issues="+issues+
      ", components="+components+
      ", rules="+rules+
      ", users="+users+
      '}';
  }

  public static SonarReport fromJson(String json) {
    return GSON.fromJson(json,SonarReport.class);
  }

}
