package org.intellij.sonar.util;

import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.SonarServerConfig;

public class SourceCodePlaceHolders {

  private String mySourceCode;
  private Project myProject;
  private SonarServerConfig mySonarServerConfig;

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

  public SourceCodePlaceHolders withSonarServerConfiguration(SonarServerConfig sonarServerConfig) {
    this.mySonarServerConfig = sonarServerConfig;
    return this;
  }

  public String build() {
    if (mySourceCode == null) throw new IllegalArgumentException("sourceCode");
    String sourceCode = mySourceCode;
    if (myProject != null) {
      sourceCode = replacePlaceHolders(sourceCode, myProject);
    }
    if (mySonarServerConfig != null) {
      sourceCode = replacePlaceHolders(sourceCode, mySonarServerConfig);
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

  public static String replacePlaceHolders(String rawSourceCode, SonarServerConfig sonarServerConfig) {
    String processedSourceCode = rawSourceCode;
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_HOST_URL\\$", sonarServerConfig.getHostUrl());
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_SERVER_NAME\\$", sonarServerConfig.getName());
    processedSourceCode = processedSourceCode.replaceAll("\\$SONAR_USER_NAME\\$", sonarServerConfig.getUser());
    return processedSourceCode;
  }
}
