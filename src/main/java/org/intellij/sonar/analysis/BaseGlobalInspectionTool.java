package org.intellij.sonar.analysis;

import com.intellij.codeInspection.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.index2.IssuesByFileIndex;
import org.intellij.sonar.util.Finders;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

abstract public class BaseGlobalInspectionTool extends GlobalSimpleInspectionTool {

  /**
   * @see com.intellij.codeInspection.InspectionEP#displayName
   * @see com.intellij.codeInspection.InspectionEP#key
   * @see com.intellij.codeInspection.InspectionEP#bundle
   */
  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "SonarQube Issue";
  }

  public abstract Boolean processIssue(IssuesByFileIndex.MyIssue issue);

  @Override
  public void checkFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, @NotNull final ProblemsHolder problemsHolder, @NotNull final GlobalInspectionContext globalContext, @NotNull final ProblemDescriptionsProcessor problemDescriptionsProcessor) {
    String path = psiFile.getVirtualFile().getPath();
    System.out.println("path one: " + path);

    final SonarQubeInspectionContext sonarQubeInspectionContext = globalContext.getExtension(SonarQubeInspectionContext.KEY);
    if (sonarQubeInspectionContext != null) {
      System.out.println("SonarQubeInspectionContext is not null ONE");
    }
    Set<IssuesByFileIndex.MyIssue> issues = IssuesByFileIndex.getIssuesForFile(path);
    for (final IssuesByFileIndex.MyIssue issue : issues) {
      if (!processIssue(issue)) continue;
      final ProblemHighlightType severity = SonarToIjSeverityMapping.toProblemHighlightType(issue.severity);
      final TextRange textRange = Finders.getLineRange(psiFile, issue.line);
      final ProblemDescriptor problemDescriptor = problemsHolder.getManager().createProblemDescriptor(psiFile, textRange,
          issue.formattedMessage(),
          severity,
          false
      );
      problemDescriptionsProcessor.addProblemElement(globalContext.getRefManager().getReference(psiFile), problemDescriptor);

    }


  }

}
