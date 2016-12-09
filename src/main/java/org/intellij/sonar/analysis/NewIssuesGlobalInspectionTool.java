package org.intellij.sonar.analysis;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.InspectionEP;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptionsProcessor;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NewIssuesGlobalInspectionTool extends BaseGlobalInspectionTool {

  private static final String SONAR_QUBE = "SonarQube";

  /**
   @see InspectionEP#groupDisplayName
   @see InspectionEP#groupKey
   @see InspectionEP#groupBundle
   */
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "SonarQube (new issues)";
  }

  /**
   Override this method to return a html inspection description. Otherwise it will be loaded from resources using ID.

   @return hard-code inspection description.
   */
  @Nullable
  @Override
  public String getStaticDescription() {
    return "Reports new issues found by SonarQube.";
  }

  @Override
  public Boolean processIssue(SonarIssue issue) {
    return issue.getIsNew();
  }

  @Override
  public void inspectionFinished(
    @NotNull InspectionManager manager,
    @NotNull GlobalInspectionContext context,
    @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor
  ) {
    super.inspectionFinished(manager,context,problemDescriptionsProcessor);
    final ImmutableList.Builder<PsiFile> pfb = ImmutableList.builder();
    context.getRefManager().getScope().accept(
      new PsiElementVisitor() {
        @Override
        public void visitFile(PsiFile file) {
          pfb.add(file);
        }
      }
    );
    final ImmutableList<PsiFile> analyzedFiles = pfb.build();
    final Set<String> analyzedPaths = analyzedFiles.stream()
        .filter(Objects::nonNull)
        .map(psiFile -> psiFile.getVirtualFile().getPath())
        .collect(Collectors.toSet());
    final Optional<IssuesByFileIndexProjectComponent> indexComponent =
      IssuesByFileIndexProjectComponent.getInstance(context.getProject());
    if (indexComponent.isPresent()) {
      final int newIssuesCount = FluentIterable.from(indexComponent.get().getIndex().entrySet())
        .filter(
            entry -> analyzedPaths.contains(entry.getKey())
        )
        .transformAndConcat(
            entry -> entry.getValue()
        )
        .filter(
            sonarIssue -> sonarIssue.getIsNew()
        ).size();
      final Notification notification;
      if (newIssuesCount == 1) {
        notification = new Notification(
          SONAR_QUBE,SONAR_QUBE,
          "Found 1 new SonarQube issue",
          NotificationType.WARNING
        );
      } else
        if (newIssuesCount > 1) {
          notification = new Notification(
            SONAR_QUBE,SONAR_QUBE,
            String.format("Found %d new SonarQube issues",newIssuesCount),
            NotificationType.WARNING
          );
        } else {
          notification = new Notification(
            SONAR_QUBE,SONAR_QUBE,
            "No new SonarQube issues",
            NotificationType.INFORMATION
          );
        }
      Notifications.Bus.notify(notification,context.getProject());
    }
  }
}
