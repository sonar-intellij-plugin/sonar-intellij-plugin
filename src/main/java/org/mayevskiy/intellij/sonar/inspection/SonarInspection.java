package org.mayevskiy.intellij.sonar.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.component.SonarComponent;
import org.mayevskiy.intellij.sonar.component.SonarModuleComponent;
import org.mayevskiy.intellij.sonar.component.SonarProjectComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;
import org.sonar.wsclient.services.Violation;

import java.util.*;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:50
 */
public class SonarInspection extends LocalInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Sonar";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SonarInspection";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "inspection from sonar";
    }

    @Nullable
    @Override
    public String getStaticDescription() {
        return "blablub";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    private Map<String, Collection<Violation>> violationsMap;

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
            result.append(".").append(javaFile.getVirtualFile().getNameWithoutExtension());
        } else {
            final VirtualFile virtualFile = file.getVirtualFile();
            final String filePath = virtualFile.getPath();

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
        return result.toString();
    }

    @Override
    public void inspectionStarted(LocalInspectionToolSession session, boolean isOnTheFly) {
        SonarSettingsBean sonarSettingsBean = getSonarSettingsBeanForFile(session.getFile());
        createViolationsMapFromSonarSettingsBean(sonarSettingsBean);
    }

    private SonarSettingsBean getSonarSettingsBeanForFile(PsiFile file) {
        SonarSettingsBean sonarSettingsBean = null;
        if (null != file) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (null != virtualFile) {
                Module module = ModuleUtil.findModuleForFile(virtualFile, file.getProject());
                if (null != module) {
                    SonarComponent component = module.getComponent(SonarModuleComponent.class);
                    sonarSettingsBean = getSonarSettingsBeanFromSonarComponent(component);
                } else {
                    sonarSettingsBean = getSonarSettingsBeanFromProject(file.getProject());
                }
            }
        }
        return sonarSettingsBean;
    }


    @Override
    public void inspectionFinished(LocalInspectionToolSession session, ProblemsHolder problemsHolder) {
        if (null != violationsMap) {
            violationsMap.clear();
        }
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        final Collection<ProblemDescriptor> result = new ArrayList<>();

        SonarSettingsBean sonarSettingsBean = getSonarSettingsBeanForFile(file);
        if (null != sonarSettingsBean) {
            String resourceKey = convertPsiFileToSonarKey(file, sonarSettingsBean.resource);
            Collection<Violation> violations = violationsMap.get(resourceKey);
            if (null != violations) {
                for (Violation violation : violations) {
                    PsiElement element = getElementAtLine(file, violation.getLine() - 1);
                    if (null != element) {
                        result.add(manager.createProblemDescriptor(
                                element,
                                violation.getMessage(),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                null,
                                isOnTheFly));
                    }
                }
            }
        }

        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private void createViolationsMapFromProject(Project project) {
        SonarSettingsBean sonarSettingsBean = getSonarSettingsBeanFromProject(project);
        createViolationsMapFromSonarSettingsBean(sonarSettingsBean);
    }

    private void createViolationsMapFromSonarSettingsBean(SonarSettingsBean sonarSettingsBean) {
        violationsMap = new HashMap<>();
        List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);
        if (null != violations) {
            for (Violation violation : violations) {
                String resourceKey = violation.getResourceKey();
                Collection<Violation> entry = violationsMap.get(resourceKey);
                if (null == entry) {
                    entry = new ArrayList<>();
                    violationsMap.put(resourceKey, entry);
                }
                entry.add(violation);
            }
        }
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        //TODO: create mapping from sonar severity
        return super.getDefaultLevel();
    }

    private SonarSettingsBean getSonarSettingsBeanFromProject(Project project) {
        SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
        return sonarProjectComponent.getState();
    }

    private SonarSettingsBean getSonarSettingsBeanFromSonarComponent(SonarComponent sonarComponent) {
        return sonarComponent.getState();
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


}
