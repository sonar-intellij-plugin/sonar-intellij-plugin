package org.mayevskiy.intellij.sonar;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:48
 */
public class SonarInspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        //TODO Get all rules from sonar server/project configuration and register it as inspections
//        ProjectManager.getInstance().getOpenProjects();
        return new Class[]{SonarInspection.class};
    }
}
