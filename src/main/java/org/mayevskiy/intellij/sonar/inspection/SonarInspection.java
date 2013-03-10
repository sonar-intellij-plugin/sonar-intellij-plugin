package org.mayevskiy.intellij.sonar.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.mayevskiy.intellij.sonar.component.SonarProjectComponent;
import org.mayevskiy.intellij.sonar.service.SonarService;
import org.sonar.wsclient.services.Violation;

import java.util.*;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:50
 */
public class SonarInspection extends BaseJavaLocalInspectionTool {
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

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        createViolationsMap(manager.getProject());

        final Collection<ProblemDescriptor> result = new ArrayList<>();

        String resourceKey = convertPsiFileToSonarKey(file, getSonarSettingsBean(manager.getProject()).resource);
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
        return result.toArray(new ProblemDescriptor[result.size()]);
    }

    private void createViolationsMap(Project project) {
        if (null == violationsMap) {
            violationsMap = new HashMap<>();
            SonarSettingsBean sonarSettingsBean = getSonarSettingsBean(project);
            List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);
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

    private SonarSettingsBean getSonarSettingsBean(Project project) {
        SonarProjectComponent sonarProjectComponent = project.getComponent(SonarProjectComponent.class);
        return sonarProjectComponent.getState();
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

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                createViolationsMap(file.getProject());

                String resourceKey = convertPsiFileToSonarKey(file, getSonarSettingsBean(file.getProject()).resource);
                Collection<Violation> violations = violationsMap.get(resourceKey);
                if (violations != null) {
                    for (Violation violation : violations) {
                        PsiElement element = getElementAtLine(file, violation.getLine() - 1);
                        if (null != element) {
                            ProblemDescriptor descriptor = holder.getManager().createProblemDescriptor(
                                    element,
                                    violation.getMessage(),
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    null,
                                    isOnTheFly);
                            holder.registerProblem(descriptor);
                        }
                    }
                }

            }
        };
    }
}
