package org.intellij.sonar.sonarserver.result;

public class Rule {

  private final String key;
  private final String repo;
  private final String severity;

  public Rule(String key,String repo,String severity) {
    this.key = key;
    this.repo = repo;
    this.severity = severity;
  }

  /**
   "key": "com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck"

   @return "com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck"
   */
  public String getKey() {
    return key;
  }

  /**
   "repo": "checkstyle"

   @return "checkstyle"
   */
  public String getRepo() {
    return repo;
  }

  /**
   "severity": "MAJOR"

   @return "MAJOR"
   */
  public String getSeverity() {
    return severity;
  }
}
