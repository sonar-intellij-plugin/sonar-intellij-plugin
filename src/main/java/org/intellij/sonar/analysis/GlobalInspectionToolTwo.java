package org.intellij.sonar.analysis;

import com.intellij.codeInspection.*;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalInspectionToolTwo extends GlobalSimpleInspectionTool {

  /**
   * @see com.intellij.codeInspection.InspectionEP#groupDisplayName
   * @see com.intellij.codeInspection.InspectionEP#groupKey
   * @see com.intellij.codeInspection.InspectionEP#groupBundle
   */
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "Two Sonar Group";
  }

  /**
   * @see com.intellij.codeInspection.InspectionEP#displayName
   * @see com.intellij.codeInspection.InspectionEP#key
   * @see com.intellij.codeInspection.InspectionEP#bundle
   */
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Two Display name";
  }

  /**
   * Override this method to return a html inspection description. Otherwise it will be loaded from resources using ID.
   *
   * @return hard-code inspection description.
   */
  @Nullable
  @Override
  public String getStaticDescription() {
    return "Two Description";
  }

  @Override
  public void checkFile(@NotNull PsiFile psiFile, @NotNull InspectionManager manager, @NotNull ProblemsHolder problemsHolder, @NotNull GlobalInspectionContext globalContext, @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    System.out.println("check file two");
    String path = psiFile.getVirtualFile().getPath();
    System.out.println("path two: " + path);

    final SonarQubeInspectionContext sonarQubeInspectionContext = globalContext.getExtension(SonarQubeInspectionContext.KEY);
    if (sonarQubeInspectionContext != null) {
      System.out.println("SonarQubeInspectionContext is not null TWO");
    }
  }
}
