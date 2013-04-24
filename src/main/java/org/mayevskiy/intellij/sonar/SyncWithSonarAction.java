package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
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
            SonarViolationsService sonarViolationsService = ServiceManager.getService(project, SonarViolationsService.class);
            if (null != sonarViolationsService) {
                sonarViolationsService.syncWithSonar(project);

               /* final InspectionManagerEx managerEx = (InspectionManagerEx) InspectionManager.getInstance(project);

                SonarInspectionProvider sonarInspectionProvider = null;
                Object[] extensions = Extensions.getExtensions("com.intellij.inspectionToolProvider");
                for (Object extension : extensions) {
                    if (extension instanceof SonarInspectionProvider) {
                        sonarInspectionProvider = (SonarInspectionProvider) extension;
                        break;
                    }
                }

                if (null != sonarInspectionProvider) {
                    for (Class sonarInspectionClass : sonarInspectionProvider.getInspectionClasses()) {
                        try {
                            SonarLocalInspectionTool inspectionTool = (SonarLocalInspectionTool) sonarInspectionClass.newInstance();
                            final PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
                            final Editor editor = LangDataKeys.EDITOR.getData(e.getDataContext());
                            AnalysisScope analysisScope = new AnalysisScope(project);

                            RunInspectionIntention.rerunInspection(inspectionTool, managerEx, analysisScope, psiFile);

                            if (null != editor) {
                                // trigger a change to force editor show on the fly hints
                                Document document = editor.getDocument();
                                int textLength = document.getTextLength();

                                final AccessToken writeAccessToken = ApplicationManager.getApplication().acquireWriteActionLock(null);

                                try {
                                    document.insertString(textLength, " ");
                                    document.deleteString(textLength, textLength + 1);
                                } finally {
                                    writeAccessToken.finish();
                                }
                            }
                        } catch (InstantiationException | IllegalAccessException e1) {
                            // skip
                        }

                    }
                }*/
            }
        }
    }
}
