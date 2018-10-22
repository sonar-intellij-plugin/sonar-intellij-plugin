package org.intellij.sonar.util;

import static org.intellij.sonar.util.Placeholder.MODULE_BASE_DIR;
import static org.intellij.sonar.util.Placeholder.MODULE_BASE_DIR_NAME;
import static org.intellij.sonar.util.Placeholder.MODULE_NAME;
import static org.intellij.sonar.util.Placeholder.PROJECT_BASE_DIR;
import static org.intellij.sonar.util.Placeholder.PROJECT_BASE_DIR_NAME;
import static org.intellij.sonar.util.Placeholder.PROJECT_NAME;
import static org.intellij.sonar.util.Placeholder.SONAR_ACCESS_TOKEN;
import static org.intellij.sonar.util.Placeholder.SONAR_HOST_URL;
import static org.intellij.sonar.util.Placeholder.SONAR_SERVER_NAME;
import static org.intellij.sonar.util.Placeholder.SONAR_USER_NAME;
import static org.intellij.sonar.util.Placeholder.SONAR_USER_PASSWORD;
import static org.intellij.sonar.util.Placeholder.WORKING_DIR;
import static org.intellij.sonar.util.Placeholder.WORKING_DIR_NAME;

import java.io.File;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.persistence.SonarServerConfig;

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
    if (myTemplate == null)
      throw new IllegalArgumentException("template");
    String template = myTemplate;
    template = replacePlaceHolders(template,myProject);
    template = replacePlaceHolders(template,myModule);
    template = replacePlaceHolders(template,mySonarServerConfig);
    template = replacePlaceHolders(template,myWorkingDir);
    return template;
  }

  private static String replacePlaceHolders(String template,File workingDir) {
    String processedTemplate = template;
    String workingDirName = "";
    String workingDirPath = "";
    if (null != workingDir) {
      workingDirName = workingDir.getName();
      workingDirPath = workingDir.getPath();
    }
    processedTemplate = processedTemplate.replace(WORKING_DIR_NAME.getVariableName(),workingDirName);
    processedTemplate = processedTemplate.replace(WORKING_DIR.getVariableName(),workingDirPath);
    return processedTemplate;
  }

  private static String replacePlaceHolders(String template,Module module) {
    String processedTemplate = template;
    String moduleName = "";
    String moduleBaseDirPath = "";
    String moduleBaseDirName = "";
    if (null != module) {
      moduleName = module.getName();
      final VirtualFile moduleFile = module.getModuleFile();
      if (null != moduleFile) {
        final VirtualFile moduleBaseDir = moduleFile.getParent();
        moduleBaseDirPath = moduleBaseDir.getPath();
        moduleBaseDirName = moduleBaseDir.getName();
      }
    }
    processedTemplate = processedTemplate.replace(MODULE_NAME.getVariableName(),moduleName);
    processedTemplate = processedTemplate.replace(MODULE_BASE_DIR_NAME.getVariableName(),moduleBaseDirName);
    processedTemplate = processedTemplate.replace(MODULE_BASE_DIR.getVariableName(),moduleBaseDirPath);
    return processedTemplate;
  }

  private static String replacePlaceHolders(String template,Project project) {
    String processedTemplate = template;
    String projectName = "";
    String projectBaseDir = "";
    String projectBaseDirName = "";
    if (null != project) {
      projectName = project.getName();
      projectBaseDir = project.getBasePath();
      projectBaseDirName = project.getBaseDir().getName();
    }
    processedTemplate = processedTemplate.replace(PROJECT_NAME.getVariableName(),projectName);
    processedTemplate = processedTemplate.replace(PROJECT_BASE_DIR_NAME.getVariableName(),projectBaseDirName);
    processedTemplate = processedTemplate.replace(PROJECT_BASE_DIR.getVariableName(),projectBaseDir);
    return processedTemplate;
  }

  private static String replacePlaceHolders(String template,SonarServerConfig sonarServerConfig) {
    String processedTemplate = template;
    String sonarUserPassword = "";
    String sonarHostUrl = "";
    String sonarServerName = "";
    String sonarUserName = "";
    String sonarAccessToken = "";
    if (null != sonarServerConfig) {
      sonarHostUrl = sonarServerConfig.getHostUrl();
      sonarServerName = sonarServerConfig.getName();
      sonarUserName = sonarServerConfig.getUser();
      if (template.contains(SONAR_USER_PASSWORD.getVariableName())
        && !sonarServerConfig.isAnonymous() && !StringUtil.isEmptyOrSpaces(sonarServerConfig.getUser())) {
        sonarUserPassword = sonarServerConfig.loadPassword();
      }
      if (!sonarServerConfig.isAnonymous() && StringUtils.isNotBlank(sonarServerConfig.loadToken())) {
        sonarAccessToken = sonarServerConfig.getToken();
      }
    }
    processedTemplate = processedTemplate.replace(SONAR_HOST_URL.getVariableName(),sonarHostUrl);
    processedTemplate = processedTemplate.replace(SONAR_SERVER_NAME.getVariableName(),sonarServerName);
    processedTemplate = processedTemplate.replace(SONAR_USER_NAME.getVariableName(),sonarUserName);
    processedTemplate = processedTemplate.replace(SONAR_USER_PASSWORD.getVariableName(),sonarUserPassword);
    processedTemplate = processedTemplate.replace(SONAR_ACCESS_TOKEN.getVariableName(),sonarAccessToken);
    return processedTemplate;
  }
}
