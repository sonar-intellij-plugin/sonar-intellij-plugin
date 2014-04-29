package org.intellij.sonar.sonarreport.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.intellij.sonar.sonarserver.result.DateTimeTypeConverter;
import org.joda.time.DateTime;

import java.util.List;

public class SonarReport {
  private String version;
  private List<Issue> issues;
  private List<Component> components;
  private List<Rule> rules;
  private List<User> users;

  public SonarReport(String version, List<Issue> issues, List<Component> components, List<Rule> rules, List<User> users) {
    this.version = version;
    this.issues = issues;
    this.components = components;
    this.rules = rules;
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

  public List<Rule> getRules() {
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

    if (components != null ? !components.equals(that.components) : that.components != null)
      return false;
    if (issues != null ? !issues.equals(that.issues) : that.issues != null)
      return false;
    if (rules != null ? !rules.equals(that.rules) : that.rules != null)
      return false;
    if (users != null ? !users.equals(that.users) : that.users != null)
      return false;
    if (version != null ? !version.equals(that.version) : that.version != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = version != null ? version.hashCode() : 0;
    result = 31 * result + (issues != null ? issues.hashCode() : 0);
    result = 31 * result + (components != null ? components.hashCode() : 0);
    result = 31 * result + (rules != null ? rules.hashCode() : 0);
    result = 31 * result + (users != null ? users.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SonarReport{" +
        "version='" + version + '\'' +
        ", issues=" + issues +
        ", components=" + components +
        ", rules=" + rules +
        ", users=" + users +
        '}';
  }

  private static final Gson gson = new GsonBuilder()
      .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
      .create();

  public static SonarReport fromJson(String json) {
    return gson.fromJson(json, SonarReport.class);
  }
}
