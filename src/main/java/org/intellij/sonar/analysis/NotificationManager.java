package org.intellij.sonar.analysis;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;

import java.util.Map;
import java.util.Set;

public class NotificationManager extends AbstractProjectComponent {

  protected NotificationManager(Project project) {
    super(project);
  }

  public void showNotificationFor(Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex) {

    final int newIssuesCount = FluentIterable.from(issuesIndex.keySet())
        .filter(new Predicate<IssuesIndexKey>() {
          @Override
          public boolean apply(IssuesIndexKey issuesIndexKey) {
            return issuesIndexKey.getIsNew();
          }
        }).toSet().size();

    if (newIssuesCount > 0) {
      String errorText = String.format("Found %d new sonar issues", newIssuesCount);
      Notifications.Bus.notify(new Notification("Sonar", "Sonar", errorText, NotificationType.ERROR), myProject);
    } else {
      Notifications.Bus.notify(new Notification("Sonar", "Sonar", "No new sonar issues", NotificationType.INFORMATION), myProject);
    }

  }

}
