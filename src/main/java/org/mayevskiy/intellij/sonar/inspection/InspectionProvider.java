package org.mayevskiy.intellij.sonar.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:48
 */
public class InspectionProvider implements InspectionToolProvider {
    @Override
    public Class[] getInspectionClasses() {
        return new Class[]{SonarInspection.class};
    }
}
