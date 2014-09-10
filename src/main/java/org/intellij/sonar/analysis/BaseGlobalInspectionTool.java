package org.intellij.sonar.analysis;

import com.intellij.codeInspection.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.index.IssuesByFileIndex;
import org.intellij.sonar.index.SonarIssue;
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

  public abstract Boolean processIssue(SonarIssue issue);

  @Override
  public void checkFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, @NotNull final ProblemsHolder problemsHolder, @NotNull final GlobalInspectionContext globalContext, @NotNull final ProblemDescriptionsProcessor problemDescriptionsProcessor) {

    Set<SonarIssue> issues = IssuesByFileIndex.getIssuesForFile(psiFile);
    for (final SonarIssue issue : issues) {
      if (!processIssue(issue)) continue;
      final ProblemHighlightType severity = SonarToIjSeverityMapping.toProblemHighlightType(issue.getSeverity());
      final TextRange textRange = Finders.getLineRange(psiFile, issue.getLine());
      final ProblemDescriptor problemDescriptor = problemsHolder.getManager().createProblemDescriptor(psiFile, textRange,
          issue.formattedMessage(),
          severity,
          false
      );
      problemDescriptionsProcessor.addProblemElement(globalContext.getRefManager().getReference(psiFile), problemDescriptor);
    }

  }

}
