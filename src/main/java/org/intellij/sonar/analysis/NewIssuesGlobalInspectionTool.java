package org.intellij.sonar.analysis;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInspection.GlobalInspectionContext;
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

import java.util.Map;
import java.util.Set;

public class NewIssuesGlobalInspectionTool extends BaseGlobalInspectionTool {

    private static final String SONAR_QUBE = "SonarQube";

    /**
     * @see com.intellij.codeInspection.InspectionEP#groupDisplayName
     * @see com.intellij.codeInspection.InspectionEP#groupKey
     * @see com.intellij.codeInspection.InspectionEP#groupBundle
     */
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "SonarQube (new issues)";
    }

    /**
     * Override this method to return a html inspection description. Otherwise it will be loaded from resources using ID.
     *
     * @return hard-code inspection description.
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
    public void inspectionFinished(@NotNull InspectionManager manager, @NotNull GlobalInspectionContext context, @NotNull ProblemDescriptionsProcessor problemDescriptionsProcessor) {
        super.inspectionFinished(manager, context, problemDescriptionsProcessor);

        final ImmutableList.Builder<PsiFile> pfb = ImmutableList.builder();
        context.getRefManager().getScope().accept(new PsiElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                pfb.add(file);
            }
        });
        final ImmutableList<PsiFile> analyzedFiles = pfb.build();
        final ImmutableSet<String> analyzedPaths = FluentIterable.from(analyzedFiles)
                .filter(new Predicate<PsiFile>() {
                    @Override
                    public boolean apply(PsiFile psiFile) {
                        return psiFile != null;
                    }
                })
                .transform(new Function<PsiFile, String>() {
                    @Override
                    public String apply(PsiFile psiFile) {
                        return psiFile.getVirtualFile().getPath();
                    }
                }).toSet();

        final Optional<IssuesByFileIndexProjectComponent> indexComponent =
                IssuesByFileIndexProjectComponent.getInstance(context.getProject());

        if (indexComponent.isPresent()) {
            final int newIssuesCount = FluentIterable.from(indexComponent.get().getIndex().entrySet())
                    .filter(new Predicate<Map.Entry<String, Set<SonarIssue>>>() {
                        @Override
                        public boolean apply(Map.Entry<String, Set<SonarIssue>> entry) {
                            return analyzedPaths.contains(entry.getKey());
                        }
                    })
                    .transformAndConcat(new Function<Map.Entry<String, Set<SonarIssue>>, Iterable<SonarIssue>>() {
                        @Override
                        public Iterable<SonarIssue> apply(Map.Entry<String, Set<SonarIssue>> entry) {
                            return entry.getValue();
                        }
                    })
                    .filter(new Predicate<SonarIssue>() {
                        @Override
                        public boolean apply(SonarIssue sonarIssue) {
                            return sonarIssue.getIsNew();
                        }
                    }).size();

            final Notification notification;
            if (newIssuesCount == 1) {
                notification = new Notification(SONAR_QUBE, SONAR_QUBE,
                        "Found 1 new SonarQube issue",
                        NotificationType.WARNING);
            } else if (newIssuesCount > 1) {
                notification = new Notification(SONAR_QUBE, SONAR_QUBE,
                        String.format("Found %d new SonarQube issues", newIssuesCount),
                        NotificationType.WARNING);
            } else {
                notification = new Notification(SONAR_QUBE, SONAR_QUBE,
                        "No new SonarQube issues",
                        NotificationType.INFORMATION);
            }
            Notifications.Bus.notify(notification, context.getProject());

        }
    }
}
