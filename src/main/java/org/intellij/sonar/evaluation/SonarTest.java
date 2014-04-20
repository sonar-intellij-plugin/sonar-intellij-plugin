package org.intellij.sonar.evaluation;

import com.google.gson.Gson;
import com.intellij.openapi.progress.util.CommandLineProgress;
import org.intellij.sonar.sonarserver.SonarServer;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.issue.Issues;
import org.sonar.wsclient.services.Profile;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.Rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class SonarTest {
  public static void main(String[] args) {
//      testGetRules();
//        testGetResources();
//    testGetAllProjectsAndModulesBySonarService();
//    testGetResourceWithProfile();
//    testGetProfile();
    testGetIssues();
  }

  private static void testGetIssues() {
    SonarServer sonarServer = SonarServer.create("https://sonar.corp.mobile.de/sonar");
    final String resourceKey = "de.mobile.dealer:dealer-admin";
//    final String resourceKey = "de.mobile:mobile-multimodule-pom";
    final Issues issues = sonarServer.getIssuesFor(resourceKey);
    System.out.println(issues.size() + " issues for " + resourceKey + " | total: " + issues.paging().total() +
    " pages: " + issues.paging().pages() + " max results reached: " + issues.maxResultsReached());
  }

  private static void testGetProfile() {
    SonarServer sonarServer = SonarServer.create("https://sonar.corp.mobile.de/sonar");
    final Profile profile = sonarServer.getProfile("java", "mobile_relaxed");
    System.out.println("rules count: " + profile.getRules().size());
  }

  private static void testGetResourceWithProfile() {
    SonarServer sonarServer = SonarServer.create("https://sonar.corp.mobile.de/sonar");
    final Resource resourceWithProfile = sonarServer.getResourceWithProfile("de.mobile:mobile-parkings-job");
    System.out.println("language: " + resourceWithProfile.getLanguage());
    System.out.println("profile: " + resourceWithProfile.getMeasure("profile").getData());
  }

  private static void testGetAllProjectsAndModulesBySonarService() {
    SonarServer sonarServer = SonarServer.create("https://sonar.corp.mobile.de/sonar");
    List<Resource> allProjectsWithModules = sonarServer.getAllProjectsAndModules();
    for (Resource projectOrModule : allProjectsWithModules) {
      if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_PROJECT)) {
        System.out.println("##################################################");
        System.out.println(projectOrModule.getName() + " (" + projectOrModule.getKey() + ")");
      } else if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
        System.out.println("     " + projectOrModule.getName() + " (" + projectOrModule.getKey() + ")");
      } else {
        System.out.println("!!!UNKNOWN RESOURCE!!! ::: " + projectOrModule.toString() + " ::: " + projectOrModule.getQualifier());
      }
    }
  }

  private static void testGetResources() {
    Sonar sonar = Sonar.create("https://sonar.corp.mobile.de/sonar");
    ResourceQuery projectResourceQuery = new ResourceQuery();
    projectResourceQuery.setQualifiers(Resource.QUALIFIER_PROJECT);
//        projectResourceQuery.
    List<Resource> projectResourceList = sonar.findAll(projectResourceQuery);
    for (Resource projectResource : projectResourceList) {
      System.out.println("##################################################");
      System.out.println(projectResource.toString() + " ::: " + projectResource.getQualifier());
      ResourceQuery moduleResourceQuery = new ResourceQuery(projectResource.getId());
      moduleResourceQuery.setDepth(-1);
      moduleResourceQuery.setQualifiers(Resource.QUALIFIER_MODULE);
      List<Resource> moduleResourceList = sonar.findAll(moduleResourceQuery);
      for (Resource moduleResource : moduleResourceList) {
        System.out.println("     " + moduleResource.toString() + " ::: " + moduleResource.getQualifier());
      }
      System.out.println("##################################################");
    }
  }
/*
  private static void testGetRules() {
    List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<SonarSettingsBean>(3);
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "org.codehaus.sonar:php-sonar-runner"));


    SonarServer sonarServer = new SonarServer();
    Collection<Rule> allRules = sonarServer.getAllRules(sonarSettingsBeans, new CommandLineProgress());
    System.out.print("foo");
  }*/
}
