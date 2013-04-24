package org.mayevskiy.intellij.sonar.evaluation;

import org.mayevskiy.intellij.sonar.SonarService;
import org.mayevskiy.intellij.sonar.SonarSettingsBean;
import org.sonar.wsclient.services.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Oleg Mayevskiy
 * Date: 24.04.13
 * Time: 13:00
 */
public class SonarTest {
    public static void main(String[] args) {
        List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<>(3);
        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
        sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));

        SonarService sonarService = new SonarService();
        List<Rule> allRules = sonarService.getAllRules(sonarSettingsBeans);
        System.out.print("bla");
    }
}
