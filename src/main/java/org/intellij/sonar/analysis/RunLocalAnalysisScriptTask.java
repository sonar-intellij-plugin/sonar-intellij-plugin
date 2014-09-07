package org.intellij.sonar.analysis;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.util.SettingsUtil;
import org.intellij.sonar.util.SourceCodePlaceHolders;

public class RunLocalAnalysisScriptTask implements Runnable {
  private final String sourceCode;
  private final String pathToSonarReport;
  private final Project project;

  public static Optional<RunLocalAnalysisScriptTask> from(Project project, Settings originSettings) {
    final Settings settings = SettingsUtil.process(project, originSettings);
    final String scripName = settings.getLocalAnalysisScripName();
    final Optional<LocalAnalysisScript> localAnalysisScript = LocalAnalysisScripts.get(scripName);
    if (!localAnalysisScript.isPresent()) return Optional.absent();
    final String rawSourceCode = localAnalysisScript.get().getSourceCode();
    final String serverName = settings.getServerName();
    final Optional<SonarServerConfig> serverConfiguration = SonarServers.get(serverName);

    final SourceCodePlaceHolders sourceCodeBuilder = SourceCodePlaceHolders.builder();
    sourceCodeBuilder.
        withSourceCode(rawSourceCode).
        withProject(project);
    if (serverConfiguration.isPresent()) {
          sourceCodeBuilder.withSonarServerConfiguration(serverConfiguration.get()).
          build();
    }
    final String sourceCode = sourceCodeBuilder.build();

    String pathToSonarReport = localAnalysisScript.get().getPathToSonarReport();

    return Optional.of(new RunLocalAnalysisScriptTask(
        project, sourceCode, pathToSonarReport
    ));
  }

  public RunLocalAnalysisScriptTask(Project project, String sourceCode, String pathToSonarReport) {
    this.project = project;
    this.sourceCode = sourceCode;
    this.pathToSonarReport = pathToSonarReport;
  }

  public void run() {
    // execute local analysis script
    System.out.println("run script: " + this.toString());
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("sourceCode", sourceCode)
        .add("pathToSonarReport", pathToSonarReport)
        .toString();
  }
}
