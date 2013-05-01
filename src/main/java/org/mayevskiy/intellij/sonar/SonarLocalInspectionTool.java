package org.mayevskiy.intellij.sonar;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.components.ServiceManager;
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
import org.sonar.wsclient.services.Violation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:50
 */
public abstract class SonarLocalInspectionTool extends LocalInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Sonar";
    }


    @NotNull
    @Override
    public String getShortName() {
        return this.getDisplayName().replaceAll("[^a-zA-Z_0-9.-]", "");
//        return this.getClass().getName().replaceAll("\\s|_|\\$","");
    }

    @Nls
    @NotNull
    @Override
    abstract public String getDisplayName();

    @NotNull
    @Override
    abstract public String getStaticDescription();

    @NotNull
    abstract public String getRuleKey();

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nullable
    private Map<String, Collection<Violation>> getViolationsMapFromPsiFile(PsiFile file) {
        Map<String, Collection<Violation>> violationsMap = null;
        if (null != file) {
            Project project = file.getProject();
            SonarViolationsProvider sonarViolationsProvider = ServiceManager.getService(project, SonarViolationsProvider.class);
            if (null != sonarViolationsProvider) {
                violationsMap = sonarViolationsProvider.mySonarViolations;
            }
        }
        return violationsMap;
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
            final VirtualFile virtualFile = javaFile.getVirtualFile();
            if (null != virtualFile) {
                result.append(".").append(virtualFile.getNameWithoutExtension());
            }
        } else {
            final VirtualFile virtualFile = file.getVirtualFile();
            if (null != virtualFile) {
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
        }
        return result.toString();
    }

    private SonarSettingsBean getSonarSettingsBeanForFile(PsiFile file) {
        SonarSettingsBean sonarSettingsBean = null;
        if (null != file) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (null != virtualFile) {
                Module module = ModuleUtil.findModuleForFile(virtualFile, file.getProject());
                if (null != module) {
                    SonarSettingsComponent component = module.getComponent(SonarSettingsModuleComponent.class);
                    sonarSettingsBean = getSonarSettingsBeanFromSonarComponent(component);
                }
            }
            if (null == sonarSettingsBean) {
                sonarSettingsBean = getSonarSettingsBeanFromProject(file.getProject());
            }
        }

        return sonarSettingsBean;
    }


    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile psiFile, @NotNull InspectionManager manager, boolean isOnTheFly) {
        final Collection<ProblemDescriptor> result = new ArrayList<>();

        SonarSettingsBean sonarSettingsBean = getSonarSettingsBeanForFile(psiFile);
        if (null != sonarSettingsBean) {
            String resourceKey = convertPsiFileToSonarKey(psiFile, sonarSettingsBean.resource);
            Map<String, Collection<Violation>> violationsMapFromPsiFile = getViolationsMapFromPsiFile(psiFile);
            Collection<Violation> violations = violationsMapFromPsiFile != null ? violationsMapFromPsiFile.get(resourceKey) : null;
            if (null != violations) {
                for (Violation violation : violations) {
                    // add only violations of this rule
                    if (this.getRuleKey().equals(violation.getRuleKey())) {
                        PsiElement element = getElementAtLine(psiFile, violation.getLine() - 1);
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
        }

        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    @NotNull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        //TODO: replace by sonar severity
        return super.getDefaultLevel();
    }

    private SonarSettingsBean getSonarSettingsBeanFromProject(Project project) {
        SonarSettingsProjectComponent sonarProjectComponent = project.getComponent(SonarSettingsProjectComponent.class);
        return sonarProjectComponent.getState();
    }

    private SonarSettingsBean getSonarSettingsBeanFromSonarComponent(SonarSettingsComponent sonarSettingsComponent) {
        return sonarSettingsComponent.getState();
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
