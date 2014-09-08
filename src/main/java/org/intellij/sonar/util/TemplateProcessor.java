package org.intellij.sonar.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.persistence.SonarServerConfig;

public class TemplateProcessor {

  private String myTemplate;
  private Project myProject;
  private Module myModule;
  private SonarServerConfig mySonarServerConfig;

  public TemplateProcessor(String myTemplate) {
    this.myTemplate = myTemplate;
  }

  public static TemplateProcessor of(String template) {
    return new TemplateProcessor(template);
  }

  public TemplateProcessor withModule(Module module) {
    this.myModule = module;
    return this;
  }

  public TemplateProcessor withProject(Project project) {
    this.myProject = project;
    return this;
  }

  public TemplateProcessor withSonarServerConfiguration(SonarServerConfig sonarServerConfig) {
    this.mySonarServerConfig = sonarServerConfig;
    return this;
  }

  public String process() {
    if (myTemplate == null) throw new IllegalArgumentException("template");
    String sourceCode = myTemplate;
    if (myProject != null) {
      sourceCode = replacePlaceHolders(sourceCode, myProject);
    }
    if (myModule != null) {
      sourceCode = replacePlaceHolders(sourceCode, myModule);
    }
    if (mySonarServerConfig != null) {
      sourceCode = replacePlaceHolders(sourceCode, mySonarServerConfig);
    }
    return sourceCode;
  }

  private String replacePlaceHolders(String template, Module module) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replaceAll("\\$MODULE_NAME\\$", module.getName());
    final VirtualFile moduleFile = module.getModuleFile();
    if (moduleFile == null) return processedTemplate;
    final VirtualFile moduleBaseDir = moduleFile.getParent();
    processedTemplate = processedTemplate.replaceAll("\\$MODULE_BASE_DIR\\$", moduleBaseDir.getPath());
    processedTemplate = processedTemplate.replaceAll("\\$MODULE_BASE_DIR_NAME\\$", moduleBaseDir.getName());
    return processedTemplate;
  }

  public static String replacePlaceHolders(String template, Project project) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replaceAll("\\$PROJECT_NAME\\$", project.getName());
    processedTemplate = processedTemplate.replaceAll("\\$PROJECT_BASE_DIR\\$", project.getBasePath());
    processedTemplate = processedTemplate.replaceAll("\\$PROJECT_BASE_DIR_NAME\\$", project.getBaseDir().getName());
    return processedTemplate;
  }

  public static String replacePlaceHolders(String template, SonarServerConfig sonarServerConfig) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replaceAll("\\$SONAR_HOST_URL\\$", sonarServerConfig.getHostUrl());
    processedTemplate = processedTemplate.replaceAll("\\$SONAR_SERVER_NAME\\$", sonarServerConfig.getName());
    processedTemplate = processedTemplate.replaceAll("\\$SONAR_USER_NAME\\$", sonarServerConfig.getUser());
    return processedTemplate;
  }
}
