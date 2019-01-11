package org.intellij.sonar.analysis;

import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.configuration.WorkingDirs;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.console.StreamGobbler;
import org.intellij.sonar.index.IssuesByFileIndexer;
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.sonarreport.data.Component;
import org.intellij.sonar.sonarreport.data.SonarReport;
import org.intellij.sonar.util.DurationUtil;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.intellij.sonar.util.SettingsUtil;
import org.intellij.sonar.util.TemplateProcessor;

public class RunLocalAnalysisScriptTask implements Runnable {

  private final String sourceCode;
  private final String pathToSonarReport;
  private final SonarQubeInspectionContext.EnrichedSettings enrichedSettings;
  private final File workingDir;
  private final SonarConsole sonarConsole;
  private final ImmutableList<PsiFile> psiFiles;

  public RunLocalAnalysisScriptTask(
    SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
    String sourceCode,
    String pathToSonarReport,
    File workingDir,
    ImmutableList<PsiFile> psiFiles
  ) {
    this.enrichedSettings = enrichedSettings;
    this.sourceCode = sourceCode;
    this.pathToSonarReport = pathToSonarReport;
    this.workingDir = workingDir;
    this.psiFiles = psiFiles;
    this.sonarConsole = SonarConsole.get(enrichedSettings.project);
  }

  public static Optional<RunLocalAnalysisScriptTask> from(
    SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
    ImmutableList<PsiFile> psiFiles
  ) {
    enrichedSettings.settings = SettingsUtil.process(enrichedSettings.project,enrichedSettings.settings);
    final String scripName = enrichedSettings.settings.getLocalAnalysisScripName();
    if (scripName == null) {
      return Optional.empty();
    }
    final Optional<LocalAnalysisScript> localAnalysisScript = LocalAnalysisScripts.get(scripName);
    if (!localAnalysisScript.isPresent())
      return Optional.empty();
    final String sourceCodeTemplate = localAnalysisScript.get().getSourceCode();
    final String serverName = enrichedSettings.settings.getServerName();
    final Optional<SonarServerConfig> serverConfiguration = SonarServers.get(serverName);
    final TemplateProcessor sourceCodeTemplateProcessor = TemplateProcessor.of(sourceCodeTemplate);
    sourceCodeTemplateProcessor
      .withProject(enrichedSettings.project)
      .withModule(enrichedSettings.module);
    String pathToSonarReportTemplate = localAnalysisScript.get().getPathToSonarReport();
    final TemplateProcessor pathToSonarReportTemplateProcessor = TemplateProcessor.of(pathToSonarReportTemplate)
      .withProject(enrichedSettings.project)
      .withModule(enrichedSettings.module);
    if (serverConfiguration.isPresent()) {
      sourceCodeTemplateProcessor.withSonarServerConfiguration(serverConfiguration.get());
      pathToSonarReportTemplateProcessor.withSonarServerConfiguration(serverConfiguration.get());
    }
    File workingDir = WorkingDirs.computeFrom(enrichedSettings);
    sourceCodeTemplateProcessor.withWorkingDir(workingDir);
    pathToSonarReportTemplateProcessor.withWorkingDir(workingDir);
    final String sourceCode = sourceCodeTemplateProcessor.process();
    final String pathToSonarReport = pathToSonarReportTemplateProcessor.process();
    return Optional.of(
      new RunLocalAnalysisScriptTask(
        enrichedSettings,sourceCode,pathToSonarReport,workingDir,
        psiFiles
      )
    );
  }

  public void run() {
    try {
      execute();
    } finally {
      sonarConsole.clearPasswordFilter();
    }
  }

  private void execute() {
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    ProgressIndicatorUtil.setText(indicator,"Executing SonarQube local analysis");
    ProgressIndicatorUtil.setText2(indicator,sourceCode);
    ProgressIndicatorUtil.setIndeterminate(indicator,true);
    final Optional<SonarServerConfig> sonarServerConfig = SonarServers.get(enrichedSettings.settings.getServerName());
    if (sonarServerConfig.isPresent()
      && !sonarServerConfig.get().isAnonymous() && !StringUtil.isEmptyOrSpaces(sonarServerConfig.get().getUser())) {
      final String password = sonarServerConfig.get().loadPassword();
      sonarConsole.withPasswordFilter(password);
    }
    if (sonarServerConfig.isPresent()
      && !sonarServerConfig.get().isAnonymous() && !StringUtil.isEmptyOrSpaces(sonarServerConfig.get().loadToken())) {
      final String token = sonarServerConfig.get().getToken();
      sonarConsole.withPasswordFilter(token);
    }
    sonarConsole.info("working dir: "+this.workingDir.getPath());
    sonarConsole.info("executing: "+this.sourceCode);
    final long startTime = System.currentTimeMillis();
    final Process process;
    try {
      process = Runtime.getRuntime().exec(this.sourceCode.split("[\\s]+"),null,this.workingDir);
    } catch (IOException e) {
      sonarConsole.error(Throwables.getStackTraceAsString(e));
      return;
    }
    final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(),sonarConsole,ERROR);
    final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),sonarConsole,INFO);
    errorGobbler.start();
    outputGobbler.start();
    while (outputGobbler.isAlive()) {
      if (indicator.isCanceled()) {
        process.destroy();
        break;
      }
    }
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      sonarConsole.info("Unexpected end of process.\n"+Throwables.getStackTraceAsString(e));
    }
    try {
      int exitCode = process.exitValue();
      sonarConsole.info(
        String.format(
          "finished with exit code %s in %s",
          exitCode,
          DurationUtil.getDurationBreakdown(System.currentTimeMillis()-startTime)
        )
      );
      if (exitCode != 0) {
        Notifications.Bus.notify(
          new Notification(
            "SonarQube","SonarQube",
            String.format("Local analysis failed (%d)",exitCode),NotificationType.WARNING
          ),enrichedSettings.project
        );
      } else {
        readIssuesFromSonarReport();
      }
    } catch (IllegalThreadStateException ite) {
      sonarConsole.info("Script execution aborted.\n"+Throwables.getStackTraceAsString(ite));
    }
  }

  private void readIssuesFromSonarReport() {
    sonarConsole.info("Reading issues from "+this.pathToSonarReport);
    String sonarReportContent;
    try {
      sonarReportContent = Files.toString(new File(pathToSonarReport),Charsets.UTF_8);
    } catch (IOException e) {
      sonarConsole.info(Throwables.getStackTraceAsString(e));
      return;
    }
    final SonarReport sonarReport = SonarReport.fromJson(sonarReportContent);
    final int issuesCount = sonarReport != null && sonarReport.getIssues() != null
      ? sonarReport.getIssues().size()
      : 0;
    if (issuesCount > 0) {
      sonarConsole.info(String.format("Found %d issues in the SonarQube report",issuesCount));
    } else {
      sonarConsole.info("Did not find any issues in the SonarQube report");
    }
    createIndexFrom(sonarReport);
  }

  private void createIndexFrom(SonarReport sonarReport) {
    final Optional<IssuesByFileIndexProjectComponent> indexComponent = IssuesByFileIndexProjectComponent.getInstance(
      enrichedSettings.project
    );
    if (!indexComponent.isPresent()) {
      return;
    }
    removeFilesAffectedByReportFromIndex(sonarReport,indexComponent);
    if (sonarReport.getIssues().size() <= 0) return;
    sonarConsole.info("Creating index from SonarQube report");
    final long indexCreationStartTime = System.currentTimeMillis();
    final Map<String,Set<SonarIssue>> index = new IssuesByFileIndexer(psiFiles)
      .withSonarReportIssues(sonarReport.getIssues())
      .withSonarConsole(sonarConsole)
      .create();
    final int issuesCount = FluentIterable.from(index.values()).transformAndConcat(
        sonarIssues -> sonarIssues
    ).size();
    sonarConsole.info(
      String.format(
        "Finished creating index from SonarQube report with %d issues in %s",
        issuesCount,
        DurationUtil.getDurationBreakdown(System.currentTimeMillis()-indexCreationStartTime)
      )
    );
    if (!index.isEmpty()) {
      final int newIssuesCount = FluentIterable.from(index.values()).transformAndConcat(
          sonarIssues -> sonarIssues
      ).filter(
          sonarIssue -> sonarIssue.getIsNew()
      ).size();
      if (newIssuesCount == 1) {
        sonarConsole.info("1 issue is new!");
      } else
        if (newIssuesCount > 1) {
          sonarConsole.info(String.format("%d issues are new!",newIssuesCount));
        }
      indexComponent.get().getIndex().putAll(index);
    }
  }

  private void removeFilesAffectedByReportFromIndex(
    SonarReport sonarReport,
    Optional<IssuesByFileIndexProjectComponent> indexComponent
  ) {
    if (sonarReport.getComponents() != null) {
      for (Component component : sonarReport.getComponents()) {
        final String path = component.getPath();
        if (path != null) {
          final String componentFullPath = new File(workingDir,path).toString();
          indexComponent.get().getIndex().remove(componentFullPath);
        }
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(RunLocalAnalysisScriptTask.class.getName())
      .add("sourceCode",sourceCode)
      .add("pathToSonarReport",pathToSonarReport)
      .toString();
  }
}
