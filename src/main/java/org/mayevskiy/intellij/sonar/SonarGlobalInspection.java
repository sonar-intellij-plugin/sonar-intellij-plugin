package org.mayevskiy.intellij.sonar;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.reference.RefElement;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Violation;

import java.util.*;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.04.13
 * Time: 11:56
 */
public class SonarGlobalInspection extends GlobalInspectionTool {
    //TODO: make dynamic
    public String getRuleKey() {
        return "grvy:org.codenarc.rule.logging.PrintlnRule";
    }

    //TODO: make dynamic
    public String getRuleName() {
        return "Println";
    }

    //TODO: make dynamic
    public String sonarSeverity() {
        return "MINOR";
    }

    //TODO: make dynamic
    @Nullable
    @Override
    public String getStaticDescription() {
        return "println should be replaced with something more robust";
    }

    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Sonar";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Sonar Inspection";
    }

    final static Key<Map<String, SonarSettingsBean>> sonarSettingsMapKey = new Key<>("sonarSettingsMap");
    final static Key<Map<String, Collection<Violation>>> sonarViolationsByFileMapKey = new Key<>("sonarViolationsByFileMap");

    @Override
    public void runInspection(AnalysisScope scope, final InspectionManager manager, final GlobalInspectionContext globalContext, final ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        putAllSonarSettingsBeansFromProjectFilesToUserData(manager, globalContext);
        createSonarViolationsByFileMapAndPutToUserData(globalContext);

        createProblemDescriptorsForRuleKeyOfThisClass(manager, globalContext, problemDescriptionsProcessor);

    }


    private void createProblemDescriptorsForRuleKeyOfThisClass(final InspectionManager manager, final GlobalInspectionContext globalContext, final ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        final Project project = manager.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        // for each sonar violation from user data
        // add problem descriptor
        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                final AccessToken readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

                try {
                    Map<String, Collection<Violation>> sonarViolations = globalContext.getUserData(sonarViolationsByFileMapKey);
                    if (null != fileOrDir && !fileOrDir.isDirectory()) {
                        PsiFile psiFile = psiManager.findFile(fileOrDir);
                        if (null != psiFile) {


                            SonarSettingsBean sonarSettingsBean = SonarSettingsUtils.getSonarSettingsBeanForFile(fileOrDir, project);
                            String resource = sonarSettingsBean.resource;
                            if (StringUtils.isNotBlank(resource)) {
                                String sonarKeyOfFile = convertPsiFileToSonarKey(psiFile, resource);

                                // get violations for sonarKey from UserData and add ProblemDescriptors
                                Collection<Violation> fileViolations = null != sonarViolations ? sonarViolations.get(sonarKeyOfFile) : null;
                                if (null != fileViolations) {
                                    // create problem descriptor for this sonar rule
                                    for (Violation violation : fileViolations) {
                                        if (null != violation && violation.getRuleKey().equals(getRuleKey())) {
                                            createProblemDescriptorForPsiFileFromSonarViolation(psiFile, violation, manager, problemDescriptionsProcessor, globalContext);
                                        }
                                    }
                                }
                            }

                        }
                    }
                } finally {
                    readAccessToken.finish();
                }
                return true;
            }
        });
    }

    private void createProblemDescriptorForPsiFileFromSonarViolation(PsiFile psiFile, Violation violation, InspectionManager manager, ProblemDescriptionsProcessor problemDescriptionsProcessor, GlobalInspectionContext globalContext) {
        PsiElement psiElement = getElementAtLine(psiFile, violation.getLine() - 1);
        if (null != psiElement) {
            boolean onTheFly = true;
            LocalQuickFix fix = null;
            ProblemHighlightType problemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(psiElement, violation.getMessage(), fix, problemHighlightType, onTheFly);
//            HintAction hintAction = new SonarHintAction();
//            ProblemDescriptor problemDescriptor = manager.createProblemDescriptor(psiElement, violation.getMessage(), problemHighlightType, hintAction, onTheFly, fix);
            RefElement refElement = globalContext.getRefManager().getReference(psiFile);
            problemDescriptionsProcessor.addProblemElement(refElement, problemDescriptor);
        }
    }

    @Nullable
    @Override
    public String getHint(QuickFix fix) {
        return "my hint";
    }

    @Nullable
    private PsiElement getElementAtLine(@NotNull PsiFile file, int line) {
        PsiElement element = null;
        final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (null != document) {
            int offset = document.getLineStartOffset(line);
            element = file.getViewProvider().findElementAt(offset);
            if (element != null && document.getLineNumber(element.getTextOffset()) != line) {
                element = element.getNextSibling();
            }
        }
        return element;
    }

    public String convertPsiFileToSonarKey(@NotNull PsiFile file, @NotNull String sonarProjectKey) {
        final StringBuilder result = new StringBuilder();
        result.append(sonarProjectKey).append(":");
        if (StdFileTypes.JAVA.equals(file.getFileType())) {
            final PsiJavaFile javaFile = (PsiJavaFile) file;
            String packageName = javaFile.getPackageName();
            if (StringUtils.isBlank(packageName)) {
                result.append("[default]");
            } else {
                result.append(packageName);
            }
            result.append(".");
            VirtualFile virtualFile = javaFile.getVirtualFile();
            if (null != virtualFile) {
                result.append(virtualFile.getNameWithoutExtension());
            }
        } else {
            final VirtualFile virtualFile = file.getVirtualFile();
            if (null != virtualFile) {
                final String filePath = virtualFile.getPath();
                if (null != filePath) {
                    final VirtualFile sourceRootForFile = ProjectRootManager.getInstance(file.getProject()).getFileIndex().getSourceRootForFile(virtualFile);
                    if (null != sourceRootForFile) {
                        final String sourceRootForFilePath = sourceRootForFile.getPath() + "/";

                        String baseFileName = filePath.replace(sourceRootForFilePath, "");

                        if (baseFileName.equals(file.getName())) {
                            result.append("[root]/");
                        }

                        result.append(baseFileName);
                    }
                }
            }
        }
        return result.toString();
    }

    private void createSonarViolationsByFileMapAndPutToUserData(GlobalInspectionContext globalContext) {
        // for each sonarSetting
        Map<String, SonarSettingsBean> sonarSettingsMap = globalContext.getUserData(sonarSettingsMapKey);
        if (null != sonarSettingsMap) {
            for (SonarSettingsBean sonarSettingsBean : sonarSettingsMap.values()) {
                // get sonar violations from sonar, if not already done, and put to user data
                List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);

                Map<String, Collection<Violation>> sonarViolationsByFileMap = globalContext.getUserData(sonarViolationsByFileMapKey);
                sonarViolationsByFileMap = createViolationsByFileMap(violations, sonarViolationsByFileMap);
                globalContext.putUserData(sonarViolationsByFileMapKey, sonarViolationsByFileMap);
            }
        }
    }

    private void putAllSonarSettingsBeansFromProjectFilesToUserData(InspectionManager manager, final GlobalInspectionContext globalContext) {
        final Project project = manager.getProject();

        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                final AccessToken readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

                try {
                    SonarSettingsBean sonarSettingsBean = SonarSettingsUtils.getSonarSettingsBeanForFile(fileOrDir, project);
                    if (null != sonarSettingsBean) {
                        Map<String, SonarSettingsBean> sonarSettingsMap = globalContext.getUserData(sonarSettingsMapKey);
                        if (null == sonarSettingsMap) {
                            sonarSettingsMap = new HashMap<>();
                        }
                        if (!sonarSettingsMap.containsKey(sonarSettingsBean.toString())) {
                            sonarSettingsMap.put(sonarSettingsBean.toString(), sonarSettingsBean);
                        }
                        globalContext.putUserData(sonarSettingsMapKey, sonarSettingsMap);
                    }
                } finally {
                    readAccessToken.finish();
                }
                return true;
            }
        });
    }

    private Map<String, Collection<Violation>> createViolationsByFileMap(List<Violation> violationsFromSonar, Map<String, Collection<Violation>> violationsMap) {
        if (null == violationsMap) {
            violationsMap = new HashMap<>();
        }

        if (null != violationsFromSonar) {
            addNewViolationsFromSonarToMap(violationsFromSonar, violationsMap);
            removeNotMoreExistingViolationsFromMap(violationsFromSonar, violationsMap);
        }
        return violationsMap;
    }


    private void addNewViolationsFromSonarToMap(List<Violation> violationsFromSonar, Map<String, Collection<Violation>> violationsMap) {
        for (Violation violation : violationsFromSonar) {
            String resourceKey = violation.getResourceKey();
            Collection<Violation> entry = violationsMap.get(resourceKey);
            if (null == entry) {
                entry = new ArrayList<>();
                violationsMap.put(resourceKey, entry);
            }

            boolean violationAlreadyExists = false;
            for (Violation alreadyExistingViolation : entry) {
                if (SonarViolationUtils.isEqual(alreadyExistingViolation, violation)) {
                    violationAlreadyExists = true;
                    break;
                }
            }
            if (!violationAlreadyExists) {
                entry.add(violation);
            }
        }
    }

    private void removeNotMoreExistingViolationsFromMap(List<Violation> violationsFromSonar, Map<String, Collection<Violation>> violationsMap) {
        Collection<Collection<Violation>> violationsCollection = violationsMap.values();

        for (Collection<Violation> violationsOfFile : violationsCollection) {
            Collection<Violation> violationsToBeRemoved = new LinkedList<>();
            for (Violation violationOfFile : violationsOfFile) {
                boolean isViolationOfFileExistsInSonar = false;
                for (Violation violationFromSonar : violationsFromSonar) {
                    if (SonarViolationUtils.isEqual(violationFromSonar, violationOfFile)) {
                        isViolationOfFileExistsInSonar = true;
                        break;
                    }
                }
                if (!isViolationOfFileExistsInSonar) {
                    violationsToBeRemoved.add(violationOfFile);
                }
            }
            violationsOfFile.removeAll(violationsToBeRemoved);
        }
    }

}
