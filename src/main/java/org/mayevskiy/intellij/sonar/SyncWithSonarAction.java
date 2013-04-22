package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 14:00
 */
public class SyncWithSonarAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (null != project) {
            SonarViolationsComponent sonarViolationsComponent = project.getComponent(SonarViolationsComponent.class);
            if (null != sonarViolationsComponent) {
                sonarViolationsComponent.syncWithSonar(project);
            }
        }
    }
}
