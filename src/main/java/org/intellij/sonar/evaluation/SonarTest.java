package org.intellij.sonar.evaluation;

import com.google.gson.Gson;
import com.intellij.openapi.progress.util.CommandLineProgress;
import org.intellij.sonar.sonarserver.SonarServer;
import org.sonar.wsclient.Sonar;
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
  }

  private static void testGetAllProjectsAndModulesBySonarService() {
    SonarServer sonarServer = new SonarServer();
    Sonar sonar = sonarServer.createSonar("https://sonar.corp.mobile.de/sonar", null, null);
    List<Resource> allProjectsWithModules = sonarServer.getAllProjectsAndModules(sonar);
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
