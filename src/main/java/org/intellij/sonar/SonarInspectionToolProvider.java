package org.intellij.sonar;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.sonar.wsclient.services.Rule;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

public class SonarInspectionToolProvider implements InspectionToolProvider {

  @Override
  public Class[] getInspectionClasses() {

    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    Collection<Class<SonarLocalInspectionTool>> classes = new LinkedList<Class<SonarLocalInspectionTool>>();
    for (Project project : openProjects) {
      SonarRulesProvider sonarRulesProviderState = ServiceManager.getService(project, SonarRulesProvider.class).getState();
      if (null != sonarRulesProviderState) {
        Collection<Rule> allRules = sonarRulesProviderState.sonarRulesByRuleKey.values();

        for (Rule rule : allRules) {
          try {
            classes.add(getSonarLocalInspectionToolForOneRule(rule));
          } catch (IllegalAccessException ignore) {
            // skip
          } catch (InstantiationException ignore) {
            // skip
          }
        }
      }
    }


    return classes.toArray(new Class[classes.size()]);
  }

  private static Class<SonarLocalInspectionTool> getSonarLocalInspectionToolForOneRule(final Rule rule) throws IllegalAccessException, InstantiationException {
    ProxyFactory f = new ProxyFactory();
    f.setSuperclass(SonarLocalInspectionTool.class);
    f.setFilter(new MethodFilter() {
      @Override
      public boolean isHandled(Method method) {
        return method.getName().equals("getDisplayName")
            || method.getName().equals("getStaticDescription")
            || method.getName().equals("getRuleKey")
            || method.getName().equals("hashCode");
      }
    });

        /*
        dunno why this is deprecated, this is better then
        object.setHandler(methodHandler);
        because we have no control over object instantiation
        */
    //noinspection deprecation
    f.setHandler(new MethodHandler() {
      String myDisplayName = rule.getTitle();
      String myStaticDescription = rule.getDescription();
      String myRuleKey = rule.getKey();

      @Override
      public Object invoke(Object o, Method method, Method method2, Object[] objects) throws Throwable {
        if (method.getName().equals("getDisplayName")) {
          return myDisplayName;
        } else if (method.getName().equals("getStaticDescription")) {
          return myStaticDescription;
        } else if (method.getName().equals("getRuleKey")) {
          return myRuleKey;
        } else if (method.getName().equals("hashCode")) {
          return null != myRuleKey ? myRuleKey.hashCode() : 0;
        } else {
          return null;
        }
      }
    });

    //noinspection unchecked
    return f.createClass();
  }
}
