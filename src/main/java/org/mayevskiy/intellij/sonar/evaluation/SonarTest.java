package org.mayevskiy.intellij.sonar.evaluation;

import org.mayevskiy.intellij.sonar.settings.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.sonarserver.SonarService;
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
    List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<SonarSettingsBean>(3);
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
//        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "org.codehaus.sonar:php-sonar-runner"));


    SonarService sonarService = new SonarService();
    Collection<Rule> allRules = sonarService.getAllRules(sonarSettingsBeans);
    System.out.print("foo");
  }
}
