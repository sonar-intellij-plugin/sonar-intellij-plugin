package org.intellij.sonar.util;

public enum Placeholder {
  WORKING_DIR,
  WORKING_DIR_NAME,
  MODULE_NAME,
  MODULE_BASE_DIR,
  MODULE_BASE_DIR_NAME,
  PROJECT_NAME,
  PROJECT_BASE_DIR,
  PROJECT_BASE_DIR_NAME,
  SONAR_HOST_URL,
  SONAR_SERVER_NAME,
  SONAR_USER_NAME,
  SONAR_USER_PASSWORD;

  public String getVariableName() {
    return "$"+this.name();
  }
}
