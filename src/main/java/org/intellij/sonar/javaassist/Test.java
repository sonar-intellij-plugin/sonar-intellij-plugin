package org.intellij.sonar.javaassist;

import com.intellij.openapi.progress.util.CommandLineProgress;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.apache.commons.lang.StringUtils;
import org.intellij.sonar.SonarLocalInspectionTool;
import org.intellij.sonar.SonarSettingsBean;
import org.intellij.sonar.sonarserver.SonarService;
import org.sonar.wsclient.services.Rule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author: Oleg Mayevskiy
 * Date: 31.03.13
 * Time: 13:52
 */
@SuppressWarnings("UnusedDeclaration")
public class Test {
  public static void main(String[] args) throws IllegalAccessException, InstantiationException {
    List<SonarSettingsBean> sonarSettingsBeans = new ArrayList<SonarSettingsBean>(3);
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:java"));
    sonarSettingsBeans.add(new SonarSettingsBean("http://localhost:9000", "admin", "admin", "java:groovy:project:groovy"));

    SonarService sonarService = new SonarService();
    Collection<Rule> allRules = sonarService.getAllRules(sonarSettingsBeans, new CommandLineProgress());
    List<Class<SonarLocalInspectionTool>> classes = new ArrayList<Class<SonarLocalInspectionTool>>(allRules.size());
    for (Rule rule : allRules) {
      classes.add(getSonarLocalInspectionToolForOneRule(rule));
    }
    for (Class clazz : classes) {
      SonarLocalInspectionTool sonarLocalInspectionTool = (SonarLocalInspectionTool) clazz.newInstance();
//            System.out.println(sonarLocalInspectionTool.getDisplayName() + " : " + sonarLocalInspectionTool.getShortName() + " : " + sonarLocalInspectionTool.getStaticDescription());
      System.out.println(clazz.getName());
    }
  }

  private static Class<SonarLocalInspectionTool> getSonarLocalInspectionToolForOneRule(final Rule rule) throws IllegalAccessException, InstantiationException {
    ProxyFactory f = new ProxyFactory();
    f.setSuperclass(SonarLocalInspectionTool.class);
    f.setFilter(new MethodFilter() {
      @Override
      public boolean isHandled(Method method) {
        return method.getName().equals("getDisplayName")
            || method.getName().equals("getStaticDescription")
            || method.getName().equals("getShortName")
            || method.getName().equals("getRuleKey");
      }
    });
    //noinspection deprecation
    f.setHandler(new MethodHandler() {
      String myDisplayName = rule.getTitle();
      String myStaticDescription = rule.getDescription();
      String myShortName = rule.getKey();
      String myRuleKey = rule.getKey();

      @Override
      public Object invoke(Object o, Method method, Method method2, Object[] objects) throws Throwable {
        if (method.getName().equals("getDisplayName")) {
          return myDisplayName;
        } else if (method.getName().equals("getStaticDescription")) {
          return myStaticDescription;
        } else if (method.getName().equals("getShortName")) {
          if (StringUtils.isNotBlank(myShortName)) {
            myShortName = myShortName.replaceAll("\\s", "");
          }
          return myShortName;
        } else if (method.getName().equals("getRuleKey")) {
          return myRuleKey;
        } else {
          return null;
        }
      }
    });

    //noinspection unchecked
    return f.createClass();
  }
}
