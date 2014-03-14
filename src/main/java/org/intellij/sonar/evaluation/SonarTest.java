package org.intellij.sonar.evaluation;

import com.google.gson.Gson;
import com.intellij.openapi.progress.util.CommandLineProgress;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import org.intellij.sonar.SonarSettingsBean;
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
    testGetIssues();
  }

  private static void testGetIssues() {
//    sonar.create(ManualMeasureCreateQuery.create("de.mobile:mobile-multimodule-pom"));
    AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    try {
      asyncHttpClient.prepareGet("https://sonar.corp.mobile.de/sonar/api/issues/search?pageSize=10&componentRoots=de.mobile:mobile-multimodule-pom").execute(new AsyncCompletionHandler<Response>(){

        @Override
        public Response onCompleted(Response response) throws Exception{
          String responseBody = response.getResponseBody();
          System.out.println(responseBody);
          Gson gson = new Gson();
          return response;
        }

        @Override
        public void onThrowable(Throwable t){
          // Something wrong happened.
        }
      });
    } catch (IOException e) {
      // something wrong with IO
    }
  }

  private static void testGetAllProjectsAndModulesBySonarService() {
    SonarServer sonarServer = new SonarServer();
    Sonar sonar = sonarServer.createSonar("https://sonar.corp.mobile.de/sonar", null, null);
    List<Resource> allProjectsWithModules = sonarServer.getAllProjectsWithModules(sonar);
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

  private static void testGetRules() {
    List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<SonarSettingsBean>(3);
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "org.codehaus.sonar:php-sonar-runner"));


    SonarServer sonarServer = new SonarServer();
    Collection<Rule> allRules = sonarServer.getAllRules(sonarSettingsBeans, new CommandLineProgress());
    System.out.print("foo");
  }
}
