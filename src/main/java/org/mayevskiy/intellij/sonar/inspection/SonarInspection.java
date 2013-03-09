package org.mayevskiy.intellij.sonar.inspection;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiUtil;
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


    // resource key my:project:de.plushnikov.TestKlasse
//            filepath E:/Projekte/Idea/sonar/src/de/plushnikov/TestKlasse.java
//            base dir E:/Projekte/Idea/sonar
    public static String convertResourceKeyToFilePath(String resourceKey) {
        String convertedKey = null;
        if (null != resourceKey) {
            convertedKey = resourceKey.substring(resourceKey.lastIndexOf((":")) + 1).replaceAll("\\.", "/");
        }
        return convertedKey;
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

        if (null == violationsMap) {
            violationsMap = new HashMap<>();
            SonarSettingsBean sonarSettingsBean = getSonarSettingsBean(manager.getProject());
            List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);
            for (Violation violation : violations) {
                String resourceKey = violation.getResourceKey();
                Collection<Violation> entry = violationsMap.get(resourceKey);
                if (null == entry) {
                    entry = new ArrayList<>();
                    violationsMap.put(resourceKey, entry);
                }
                entry.add(violation);

                violation.getResourceKey();
                violation.getResourceQualifier();
            }
            // resource key my:project:de.plushnikov.TestKlasse
//            filepath E:/Projekte/Idea/sonar/src/de/plushnikov/TestKlasse.java
//            base dir E:/Projekte/Idea/sonar
        }


//        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(ModuleUtil.findModuleForPsiElement(file)).getSourceRoots();
//        ProjectRootManager.getInstance(file.getProject()).getFileIndex().getSourceRootForFile(file.getVirtualFile())
//        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
//        CreateClassUtil.obtainDirectoryRootForPackage(module, "my.package.name");
//        ModuleRootManager rootManager = ModuleRootManager.getInstance(ModuleUtil.findModuleForPsiElement(file));
//        VirtualFile[] contentRoots = rootManager.getContentRoots();


        final Collection<ProblemDescriptor> result = new ArrayList<>();
        final FileDocumentManager documentManager = FileDocumentManager.getInstance();
        Document document = documentManager.getDocument(file.getVirtualFile());

        String resourceKey = convertPsiFileToSonarKey(file, getSonarSettingsBean(manager.getProject()).resource);
        Collection<Violation> violations = violationsMap.get(resourceKey);
        if (null != violations) {
            for (Violation violation : violations) {
                if (null != document && violation.getLine() < document.getLineCount()) {
                    final int startOffset = document.getLineStartOffset(violation.getLine());
                    PsiElement element = PsiUtil.getElementAtOffset(file, startOffset);

//                final String message = violation.getRuleName();
                    String message = violation.getMessage();
                    ProblemDescriptor descriptor = manager.createProblemDescriptor(
                            element,
                            message,
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            null,
                            isOnTheFly);

                    result.add(descriptor);
                }
            }
        }


        return result.toArray(new ProblemDescriptor[result.size()]);
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

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                if (null == violationsMap) {
                    violationsMap = new HashMap<>();
                    SonarSettingsBean sonarSettingsBean = getSonarSettingsBean(file.getProject());
                    List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);
                    for (Violation violation : violations) {
                        String resourceKey = violation.getResourceKey();
                        Collection<Violation> entry = violationsMap.get(resourceKey);
                        if (null == entry) {
                            entry = new ArrayList<>();
                            violationsMap.put(resourceKey, entry);
                        }
                        entry.add(violation);

                        violation.getResourceKey();
                        violation.getResourceQualifier();
                    }
                }

                final FileDocumentManager documentManager = FileDocumentManager.getInstance();
                Document document = documentManager.getDocument(file.getVirtualFile());

                String resourceKey = convertPsiFileToSonarKey(file, getSonarSettingsBean(file.getProject()).resource);
                Collection<Violation> violations = violationsMap.get(resourceKey);
                if (violations != null) {
                    for (Violation violation : violations) {
                        if (null != document && violation.getLine() <= document.getLineCount()) {
                            final int lineStartOffset = document.getLineStartOffset(violation.getLine() - 1);
                            final int lineEndOffset = document.getLineEndOffset(violation.getLine() - 1);
                            PsiElement elementStart = PsiUtil.getElementAtOffset(file, lineStartOffset);
                            PsiElement elementEnd = PsiUtil.getElementAtOffset(file, lineEndOffset);

                            ProblemDescriptor descriptor = holder.getManager().createProblemDescriptor(
                                    elementStart,
                                    elementEnd,
                                    violation.getMessage(),
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                    isOnTheFly);

                            holder.registerProblem(descriptor);
                        }
                    }
                }

            }
        };
    }
}
