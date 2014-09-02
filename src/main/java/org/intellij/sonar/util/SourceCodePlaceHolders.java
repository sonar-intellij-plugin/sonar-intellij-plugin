package org.intellij.sonar.util;

import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.SonarServerConfiguration;

public class SourceCodePlaceHolders {

  private String mySourceCode;
  private Project myProject;
  private SonarServerConfiguration mySonarServerConfiguration;

  public static SourceCodePlaceHolders builder() {
    return new SourceCodePlaceHolders();
  }

  public SourceCodePlaceHolders withSourceCode(String sourceCode) {
    this.mySourceCode = sourceCode;
    return this;
  }

  public SourceCodePlaceHolders withProject(Project project) {
    this.myProject = project;
    return this;
  }

  public SourceCodePlaceHolders withSonarServerConfiguration(SonarServerConfiguration sonarServerConfiguration) {
    this.mySonarServerConfiguration = sonarServerConfiguration;
    return this;
  }

  public String build() {
    if (mySourceCode == null) throw new IllegalArgumentException("sourceCode");
    String sourceCode = mySourceCode;
    if (myProject != null) {
      sourceCode = replacePlaceHolders(sourceCode, myProject);
    }
    if (mySonarServerConfiguration != null) {
      sourceCode = replacePlaceHolders(sourceCode, mySonarServerConfiguration);
    }
    return sourceCode;
  }

  public static String replacePlaceHolders(String rawSourceCode, Project project) {
    String processedSourceCode = rawSourceCode;
    processedSourceCode = processedSourceCode.replaceAll("\\$PROJECT_NAME\\$", project.getName());
    processedSourceCode = processedSourceCode.replaceAll("\\$PROJECT_BASE_DIR\\$", project.getBasePath());
    processedSourceCode = processedSourceCode.replaceAll("\\$PROJECT_BASE_DIR_NAME\\$", project.getBaseDir().getName());
    return processedSourceCode;
  }

  public static String replacePlaceHolders(String rawSourceCode, SonarServerConfiguration sonarServerConfiguration) {
    String processedSourceCode = rawSourceCode;
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_HOST_URL\\$", sonarServerConfiguration.getHostUrl());
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_SERVER_NAME\\$", sonarServerConfiguration.getName());
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_USER_NAME\\$", sonarServerConfiguration.getUser());
    return processedSourceCode;
  }
}
