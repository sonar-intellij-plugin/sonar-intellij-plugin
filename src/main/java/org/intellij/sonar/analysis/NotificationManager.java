package org.intellij.sonar.analysis;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.index2.SonarIssue;

import java.util.Map;
import java.util.Set;

public class NotificationManager extends AbstractProjectComponent {

  public static NotificationManager getInstance(Project project) {
    return project.getComponent(NotificationManager.class);
  }

  protected NotificationManager(Project project) {
    super(project);
  }

  public void showNotificationFor(Map<String, Set<SonarIssue>> issuesIndex) {

    final Optional<SonarIssue> anyNewIssues = FluentIterable.from(issuesIndex.values())
        .transformAndConcat(new Function<Set<SonarIssue>, Iterable<SonarIssue>>() {
          @Override
          public Iterable<SonarIssue> apply(Set<SonarIssue> sonarIssues) {
            return sonarIssues;
          }
        }).firstMatch(new Predicate<SonarIssue>() {
          @Override
          public boolean apply(SonarIssue sonarIssue) {
            return sonarIssue.getIsNew();
          }
        });

    if (anyNewIssues.isPresent()) {
      String errorText = String.format("Found new sonar issues");
      Notifications.Bus.notify(new Notification("Sonar", "Sonar", errorText, NotificationType.WARNING), myProject);
    } else {
      Notifications.Bus.notify(new Notification("Sonar", "Sonar", "No new sonar issues", NotificationType.INFORMATION), myProject);
    }

  }

}
