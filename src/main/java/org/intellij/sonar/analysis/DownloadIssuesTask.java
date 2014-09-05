package org.intellij.sonar.analysis;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DownloadIssuesTask extends Task.Backgroundable {
  private final String hostName;
  private final String user;
  private final String password;
  private final Set<String> resourceKeys;

  public DownloadIssuesTask(Project project, String hostName, String user, String password, Set<String> resourceKeys) {
    super(project, "Download Sonar Issues");
    this.hostName = hostName;
    this.user = user;
    this.password = password;
    this.resourceKeys = resourceKeys;
  }

  @Override
  public void run(@NotNull ProgressIndicator indicator) {
    // download issues
  }

  @Override
  public void onSuccess() {
    super.onSuccess();
    // write downloaded issues to index
  }

  @Override
  public void onCancel() {
    super.onCancel();
    // abort downloading
  }
}
