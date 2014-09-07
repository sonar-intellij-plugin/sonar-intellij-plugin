package org.intellij.sonar;

import com.intellij.codeInspection.InspectionToolProvider;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.intellij.sonar.analysis.SonarLocalInspectionTool;
import org.intellij.sonar.persistence.SonarRuleBean;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

public class SonarInspectionToolProvider implements InspectionToolProvider {

  public static Collection<Class<SonarLocalInspectionTool>> classes = new LinkedList<Class<SonarLocalInspectionTool>>();

  @Override
  public Class[] getInspectionClasses() {
    /*Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    for (Project project : openProjects) {
      Optional<SonarRules> sonarRulesComponent = fromNullable(ServiceManager.getService(project, SonarRules.class));
      if (sonarRulesComponent.isPresent()) {
        Collection<SonarRuleBean> allRules = sonarRulesComponent.get().getSonarRulesByRuleKey().values();

        for (SonarRuleBean rule : allRules) {
          try {
            classes.add(getSonarLocalInspectionToolForOneRule(rule, false));
            classes.add(getSonarLocalInspectionToolForOneRule(rule, true));
          } catch (IllegalAccessException ignore) {
          } catch (InstantiationException ignore) {
          }
        }
      }
    }*/

    return classes.toArray(new Class[classes.size()]);
  }

  private static Class<SonarLocalInspectionTool> getSonarLocalInspectionToolForOneRule(final SonarRuleBean rule, final boolean isNew) throws IllegalAccessException, InstantiationException {
    ProxyFactory f = new ProxyFactory();
    f.setSuperclass(SonarLocalInspectionTool.class);
    f.setFilter(new MethodFilter() {
      @Override
      public boolean isHandled(Method method) {
        return method.getName().equals("getGroupDisplayName")
            || method.getName().equals("isNew")
            || method.getName().equals("getDisplayName")
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
      String myDisplayName = rule.getDisplayName();
      String myStaticDescription = rule.getDescription();
      String myRuleKey = rule.getKey();

      @Override
      public Object invoke(Object o, Method method, Method method2, Object[] objects) throws Throwable {
        if (method.getName().equals("getGroupDisplayName")) {
          return isNew ? "Sonar (New issues)" : "Sonar";
        } else if (method.getName().equals("isNew")) {
          return isNew;
        } else if (method.getName().equals("getDisplayName")) {
          return isNew ? myDisplayName + " (New issue)" : myDisplayName;
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
