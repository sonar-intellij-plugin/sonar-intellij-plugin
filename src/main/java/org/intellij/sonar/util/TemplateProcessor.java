package org.intellij.sonar.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.persistence.SonarServerConfig;

import java.io.File;

import static org.intellij.sonar.util.Placeholder.*;

public class TemplateProcessor {

  private String myTemplate;
  private Project myProject;
  private Module myModule;
  private SonarServerConfig mySonarServerConfig;
  private File myWorkingDir;

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

  public TemplateProcessor withWorkingDir(File workingDir) {
    this.myWorkingDir = workingDir;
    return this;
  }

  public String process() {
    if (myTemplate == null) throw new IllegalArgumentException("template");
    String template = myTemplate;
    if (myProject != null) {
      template = replacePlaceHolders(template, myProject);
    }
    if (myModule != null) {
      template = replacePlaceHolders(template, myModule);
    }
    if (mySonarServerConfig != null) {
      template = replacePlaceHolders(template, mySonarServerConfig);
    }
    if (myWorkingDir != null) {
      template = replacePlaceHolders(template, myWorkingDir);
    }
    return template;
  }

  private String replacePlaceHolders(String template, File workingDir) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replace(WORKING_DIR.getVariableName(), workingDir.getPath());
    processedTemplate = processedTemplate.replace(WORKING_DIR_NAME.getVariableName(), workingDir.getName());
    return processedTemplate;
  }

  private String replacePlaceHolders(String template, Module module) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replace(MODULE_NAME.getVariableName(), module.getName());
    final VirtualFile moduleFile = module.getModuleFile();
    if (moduleFile == null) return processedTemplate;
    final VirtualFile moduleBaseDir = moduleFile.getParent();
    processedTemplate = processedTemplate.replace(MODULE_BASE_DIR.getVariableName(), moduleBaseDir.getPath());
    processedTemplate = processedTemplate.replace(MODULE_BASE_DIR_NAME.getVariableName(), moduleBaseDir.getName());
    return processedTemplate;
  }

  public static String replacePlaceHolders(String template, Project project) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replace(PROJECT_NAME.getVariableName(), project.getName());
    processedTemplate = processedTemplate.replace(PROJECT_BASE_DIR.getVariableName(), project.getBasePath());
    processedTemplate = processedTemplate.replace(PROJECT_BASE_DIR_NAME.getVariableName(), project.getBaseDir().getName());
    return processedTemplate;
  }

  public static String replacePlaceHolders(String template, SonarServerConfig sonarServerConfig) {
    String processedTemplate = template;
    processedTemplate = processedTemplate.replace(SONAR_HOST_URL.getVariableName(), sonarServerConfig.getHostUrl());
    processedTemplate = processedTemplate.replace(SONAR_SERVER_NAME.getVariableName(), sonarServerConfig.getName());
    processedTemplate = processedTemplate.replace(SONAR_USER_NAME.getVariableName(), sonarServerConfig.getUser());
    if (template.contains(SONAR_USER_PASSWORD.getVariableName())) {
      processedTemplate = processedTemplate.replace(SONAR_USER_PASSWORD.getVariableName(), sonarServerConfig.loadPassword());
    }
    return processedTemplate;
  }
}
