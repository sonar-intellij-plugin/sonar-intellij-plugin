package org.intellij.sonar.analysis;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
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
import org.intellij.sonar.persistence.IssuesByFileIndexProjectService;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.intellij.sonar.persistence.LocalAnalysisScripts;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.sonarreport.data.Component;
import org.intellij.sonar.sonarreport.data.SonarReport;
import org.intellij.sonar.util.DurationUtil;
import org.intellij.sonar.util.ProgressIndicatorUtil;
import org.intellij.sonar.util.TemplateProcessor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

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
    return Optional.ofNullable(new RunLocalAnalysisScriptTaskFactory().create(enrichedSettings, psiFiles));
  }

  private static class RunLocalAnalysisScriptTaskFactory {

    private TemplateProcessor sourceCodeTemplateProcessor;
    private TemplateProcessor pathToSonarReportTemplateProcessor;
    private SonarQubeInspectionContext.EnrichedSettings enrichedSettings;
    private LocalAnalysisScript localAnalysisScript;
    private File workingDir;

    RunLocalAnalysisScriptTask create(
            SonarQubeInspectionContext.EnrichedSettings enrichedSettings,
            ImmutableList<PsiFile> psiFiles
    ) {
      this.enrichedSettings = enrichedSettings;
      if (enrichedSettings.settings != null) {
        this.enrichedSettings.settings = enrichedSettings.settings.enrichWithProjectSettings(enrichedSettings.project);
        // check scriptName
        final String scripName = enrichedSettings.settings.getLocalAnalysisScripName();
        if (scripName == null) {
          return null;
        }
        // check LocalAnalysisScript
        localAnalysisScript = LocalAnalysisScripts.get(scripName).orElse(null);
        if (localAnalysisScript == null)
          return null;
      }

      initSourceCodeTemplateProcessor();
      initPathToSonarReportTemplateProcessor();

      addServerConfigurationForProcessing();
      addWorkingDirForProcessing();

      // execute processors
      final String sourceCode = sourceCodeTemplateProcessor.process();
      final String pathToSonarReport = pathToSonarReportTemplateProcessor.process();

      return new RunLocalAnalysisScriptTask(
              enrichedSettings,
              sourceCode,
              pathToSonarReport,
              workingDir,
              psiFiles
      );
    }

    private void initSourceCodeTemplateProcessor() {
      final String sourceCodeTemplate = localAnalysisScript.getSourceCode();
      sourceCodeTemplateProcessor = TemplateProcessor.of(sourceCodeTemplate);
      sourceCodeTemplateProcessor
              .withProject(enrichedSettings.project)
              .withModule(enrichedSettings.module);
    }

    private void initPathToSonarReportTemplateProcessor() {
      String pathToSonarReportTemplate = localAnalysisScript.getPathToSonarReport();
      pathToSonarReportTemplateProcessor = TemplateProcessor.of(pathToSonarReportTemplate)
              .withProject(enrichedSettings.project)
              .withModule(enrichedSettings.module);
    }

    private void addServerConfigurationForProcessing() {
      final String serverName = enrichedSettings.settings.getServerName();
      final Optional<SonarServerConfig> serverConfiguration = SonarServers.get(serverName);
      if (serverConfiguration.isPresent()) {
        sourceCodeTemplateProcessor.withSonarServerConfiguration(serverConfiguration.get());
        pathToSonarReportTemplateProcessor.withSonarServerConfiguration(serverConfiguration.get());
      }
    }

    private void addWorkingDirForProcessing() {
      workingDir = WorkingDirs.computeFrom(enrichedSettings);
      sourceCodeTemplateProcessor.withWorkingDir(workingDir);
      pathToSonarReportTemplateProcessor.withWorkingDir(workingDir);
    }
  }

  public void run() {
    try {
      execute();
    } finally {
      sonarConsole.clearPasswordFilter();
    }
  }

  private void execute() {

    // init indicator
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    ProgressIndicatorUtil.setText(indicator,"Executing SonarQube local analysis");
    ProgressIndicatorUtil.setText2(indicator,sourceCode);
    ProgressIndicatorUtil.setIndeterminate(indicator,true);

    final Optional<SonarServerConfig> sonarServerConfig = SonarServers.get(enrichedSettings.settings.getServerName());
    if (sonarServerConfig.isPresent() && useLoginPassword(sonarServerConfig.get())) {
      final String password = sonarServerConfig.get().loadPassword();
      sonarConsole.withPasswordFilter(password);
    }

    if (sonarServerConfig.isPresent() && useToken(sonarServerConfig.get())) {
      final String token = sonarServerConfig.get().getToken();
      sonarConsole.withPasswordFilter(token);
    }

    // execute user defined local analysis script
    final long startTime = System.currentTimeMillis();
    sonarConsole.info("working dir: " + this.workingDir.getPath());
    sonarConsole.info("executing: " + this.sourceCode);
    final Process process;
    try {
      process = Runtime.getRuntime().exec(this.sourceCode.split("[\\s]+"),null,this.workingDir);
    } catch (IOException e) {
      sonarConsole.error(Throwables.getStackTraceAsString(e));
      return;
    }

    streamProcessOutputToSonarConsole(indicator, process);

    waitForProcessEnd(process);
    logAnalysisResultToConsole(startTime, process);

  }

  private boolean useLoginPassword(SonarServerConfig sonarServerConfig) {
    return !sonarServerConfig.isAnonymous() && !StringUtil.isEmptyOrSpaces(sonarServerConfig.getUser());
  }

  private boolean useToken(SonarServerConfig sonarServerConfig) {
    return !sonarServerConfig.isAnonymous() && !StringUtil.isEmptyOrSpaces(sonarServerConfig.loadToken());
  }

  private void streamProcessOutputToSonarConsole(ProgressIndicator indicator, Process process) {
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
  }

  private void waitForProcessEnd(Process process) {
    try {
      process.waitFor();
    } catch (InterruptedException e) {
      sonarConsole.info("Unexpected end of process.\n"+ Throwables.getStackTraceAsString(e));
    }
  }

  private void logAnalysisResultToConsole(long startTime, Process process) {
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
            String.format("Local analysis failed (%d)",exitCode), NotificationType.WARNING
          ),enrichedSettings.project
        );
      } else {
        readIssuesFromSonarReport();
      }
    } catch (IllegalThreadStateException ite) {
      sonarConsole.info("Script execution aborted.\n"+ Throwables.getStackTraceAsString(ite));
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
    if (sonarReport != null) {
      createIndexFrom(sonarReport);
    }
  }

  private void createIndexFrom(SonarReport sonarReport) {
    final Optional<IssuesByFileIndexProjectService> indexComponent = IssuesByFileIndexProjectService.getInstance(
      enrichedSettings.project
    );

    // check if index component exists
    if (!indexComponent.isPresent()) {
      return;
    }

    removeFilesAffectedByReportFromIndex(sonarReport,indexComponent.get());

    // do nothing if no sonar issues
    if (sonarReport.getIssues().isEmpty()) return;

    // create index
    sonarConsole.info("Creating index from SonarQube report");
    final long indexCreationStartTime = System.currentTimeMillis();
    final Map<String,Set<SonarIssue>> index = new IssuesByFileIndexer(psiFiles)
      .withSonarReportIssues(sonarReport.getIssues())
      .withSonarConsole(sonarConsole)
      .create();

    logIndexCreationToConsole(indexCreationStartTime, index);
    if (!index.isEmpty()) {
      logNewIssuesToConsole(index);
      indexComponent.get().getIndex().putAll(index);
    }
  }

  private void logIndexCreationToConsole(long indexCreationStartTime, Map<String, Set<SonarIssue>> index) {
    final int issuesCount = index.values().stream()
            .mapToInt(Set::size)
            .sum();
    sonarConsole.info(
      String.format(
        "Finished creating index from SonarQube report with %d issues in %s",
        issuesCount,
        DurationUtil.getDurationBreakdown(System.currentTimeMillis()-indexCreationStartTime)
      )
    );
  }

  private void logNewIssuesToConsole(Map<String, Set<SonarIssue>> index) {
    final long newIssuesCount = index.values().stream()
            .flatMap(Collection::stream)
            .filter(SonarIssue::getIsNew)
            .count();
    if (newIssuesCount == 1) {
      sonarConsole.info("1 issue is new!");
    } else if (newIssuesCount > 1) {
        sonarConsole.info(String.format("%d issues are new!",newIssuesCount));
    }
  }

  private void removeFilesAffectedByReportFromIndex(
    SonarReport sonarReport,
    IssuesByFileIndexProjectService indexComponent
  ) {
    if (sonarReport.getComponents() != null) {
      for (Component component : sonarReport.getComponents()) {
        removeComponentFromIndex(indexComponent, component);
      }
    }
  }

  private void removeComponentFromIndex(IssuesByFileIndexProjectService indexComponent, Component component) {
    final String path = component.getPath();
    if (path != null) {
      final String componentFullPath = new File(workingDir,path).toString();
      indexComponent.getIndex().remove(componentFullPath);
    }
  }

  @Override
  public String toString() {
    return "RunLocalAnalysisScriptTask{" +
            "sourceCode='" + sourceCode + '\'' +
            ", pathToSonarReport='" + pathToSonarReport + '\'' +
            '}';
  }
}
