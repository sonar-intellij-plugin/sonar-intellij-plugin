package org.intellij.sonar.util;

import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;

public class SonarComponentToFileMatcher {

  private boolean match;
  private boolean processing;
  private String normalizedFileKey;
  private String componentFromSonar;
  private String resourceKeyFromConfiguration;
  private String fullFilePathFromLocalFileSystem;
  private String fileKeyFromComponent;

  /**
   * matches sonar resource key to file path
   * @param componentFromSonar like sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java
   * @param resourceKeyFromConfiguration like sonar:project
   * @param fullFilePathFromLocalFileSystem like /path/to/a.file
   * @return true if the sonar component corresponds to the file, false if not
   */
  public boolean match(
    String componentFromSonar,
    String resourceKeyFromConfiguration,
    String fullFilePathFromLocalFileSystem
  ) {
    match = false;
    processing = true;
    checkNotNullOrEmpty(componentFromSonar, fullFilePathFromLocalFileSystem);
    this.componentFromSonar = componentFromSonar;
    this.resourceKeyFromConfiguration = resourceKeyFromConfiguration;
    this.fullFilePathFromLocalFileSystem = fullFilePathFromLocalFileSystem;
    if (processing) checkSonarComponentStartsWithResourceKeyFromConfiguration();
    if (processing) matchByFullPath();
    if (processing) matchByRootFileName();
    if (processing) matchByJavaClassFileName();
    return match;
  }

  private void checkNotNullOrEmpty(String componentFromSonar, String fullFilePathFromLocalFileSystem) {
    if (isEmptyOrSpaces(componentFromSonar)
        || isEmptyOrSpaces(fullFilePathFromLocalFileSystem)) {
      match = false;
      processing = false;
    }
  }

  /**
   * checks if component starts with resource key, like:
   * component = "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java";
   * resource key = "sonar:project"
   */
  private void checkSonarComponentStartsWithResourceKeyFromConfiguration() {
    if (!isEmptyOrSpaces(this.resourceKeyFromConfiguration) && !this.componentFromSonar.startsWith
      (this.resourceKeyFromConfiguration)) {
      match = false;
      processing = false;
    }
  }

  /**
   * matches by full path, e.g. if
   * file key is  "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java"
   * and full file path on file system is "src/main/java/org/sonar/batch/DefaultSensorContext.java"
   */
  private void matchByFullPath() {
    if (!isEmptyOrSpaces(resourceKeyFromConfiguration)) {
      fileKeyFromComponent = this.componentFromSonar.replace(resourceKeyFromConfiguration +":","");
    } else {
      fileKeyFromComponent = this.componentFromSonar.replaceAll("(?i)(.+:)(.+)","$2");
    }
    if (this.fullFilePathFromLocalFileSystem.endsWith(fileKeyFromComponent)) {
      match = true;
      processing = false;
    }
  }

  /**
   * matches files in root dir, e.g. if
   * file key is "[root]/VeryBadClassRoot.groovy"
   * and full file path on file system is "/VeryBadClassRoot.groovy"
   */
  private void matchByRootFileName() {

    normalizedFileKey = fileKeyFromComponent.replaceAll("\\[.+\\]","");
    if (this.fullFilePathFromLocalFileSystem.endsWith(normalizedFileKey)) {
      match = true;
      processing = false;
    }
  }

  /**
   * matches classes without .java suffix, e.g. if
   * file key is ".OtherClass"
   * and full file path ends with "/OtherClass.java"
   */
  private void matchByJavaClassFileName() {
    final String normalizedFileKeyForJava;
    normalizedFileKeyForJava = normalizedFileKey.replaceAll("\\.","/")+".java";
    if (this.fullFilePathFromLocalFileSystem.endsWith(normalizedFileKeyForJava)) {
      match = true;
      processing = false;
    }
  }
}
