package org.intellij.sonar.evaluation;

import com.intellij.openapi.progress.util.CommandLineProgress;
import org.intellij.sonar.SonarSettingsBean;
import org.intellij.sonar.sonarserver.SonarService;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.Rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Oleg Mayevskiy
 * Date: 24.04.13
 * Time: 13:00
 */
@SuppressWarnings("UnusedDeclaration")
public class SonarTest {
  public static void main(String[] args) {
//      testGetRules();
//        testGetResources();
    testGetAllProjectsAndModulesBySonarService();
  }

  private static void testGetAllProjectsAndModulesBySonarService() {
    SonarService sonarService = new SonarService();
    Sonar sonar = sonarService.createSonar("https://sonar.corp.mobile.de/sonar", null, null);
    List<Resource> allProjectsWithModules = sonarService.getAllProjectsWithModules(sonar);
    for (Resource projectOrModule : allProjectsWithModules) {
      if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_PROJECT)) {
        System.out.println("##################################################");
        System.out.println(projectOrModule.getName() + " ("+projectOrModule.getKey()+")");
      } else if (projectOrModule.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
        System.out.println("     " + projectOrModule.getName() + " ("+projectOrModule.getKey()+")");
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

  private static void testGetRules() {
    List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<SonarSettingsBean>(3);
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "org.codehaus.sonar:php-sonar-runner"));


    SonarService sonarService = new SonarService();
    Collection<Rule> allRules = sonarService.getAllRules(sonarSettingsBeans, new CommandLineProgress());
    System.out.print("foo");
  }
}
