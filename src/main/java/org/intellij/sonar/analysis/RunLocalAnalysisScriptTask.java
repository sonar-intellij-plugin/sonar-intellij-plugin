package org.intellij.sonar.analysis;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RunLocalAnalysisScriptTask extends Task.Backgroundable {
  private final String sourceCode;
  private final String pathToSonarReport;

  public RunLocalAnalysisScriptTask(Project project, String sourceCode, String pathToSonarReport) {
    super(project, "Run Local Analysis");
    this.sourceCode = sourceCode;
    this.pathToSonarReport = pathToSonarReport;
  }

  @Override
  public void run(@NotNull ProgressIndicator indicator) {
    // execute local analysis script
  }

  @Override
  public void onCancel() {
    super.onCancel();
    // abort running script
  }

  @Override
  public void onSuccess() {
    super.onSuccess();
    // write results from pathToSonarReport to issues index
  }
}
