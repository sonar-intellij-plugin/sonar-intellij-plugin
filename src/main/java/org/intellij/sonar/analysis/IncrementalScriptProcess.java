package org.intellij.sonar.analysis;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ConcurrentHashSet;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.console.StreamGobbler;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.FluentIterable.from;
import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

public class IncrementalScriptProcess {

  private final static Logger LOG = Logger.getInstance(IncrementalScriptProcess.class);

  private final static ConcurrentHashSet<IncrementalScriptProcess> runningProcesses = new ConcurrentHashSet<IncrementalScriptProcess>();

  private final IncrementalScriptBean incrementalScriptBean;
  private final String workingDirectory;
  private final SonarConsole console;
  private Stopwatch stopwatch;
  private Process process;
  private StreamGobbler errorGobbler;
  private StreamGobbler outputGobbler;
  private Optional<String> filePath;
  private boolean restarting;

  public static IncrementalScriptProcess of(IncrementalScriptBean incrementalScriptBean, String workingDirectory, Project project) {
    final IncrementalScriptProcess newIncrementalScriptProcess = new IncrementalScriptProcess(incrementalScriptBean, workingDirectory, project);
    final Optional<IncrementalScriptProcess> runningIncrementalScriptProcess = FluentIterable.from(runningProcesses).firstMatch(new Predicate<IncrementalScriptProcess>() {
      @Override
      public boolean apply(IncrementalScriptProcess runningProcess) {
        return runningProcess.equals(newIncrementalScriptProcess);
      }
    });
    return runningIncrementalScriptProcess.isPresent() ? runningIncrementalScriptProcess.get(): newIncrementalScriptProcess;
  }

  private IncrementalScriptProcess(IncrementalScriptBean incrementalScriptBean, String workingDirectory, Project project) {
    this.incrementalScriptBean = incrementalScriptBean;
    this.workingDirectory = workingDirectory;
    this.console = SonarConsole.get(project);
    this.filePath = Optional.absent();
    this.restarting = false;
  }

  public static Set<IncrementalScriptProcess> getAllRunningProcesses() {
    return runningProcesses;
  }

  public IncrementalScriptProcess onChangeOf(@NotNull String filePath) {
    this.filePath = Optional.of(filePath);
    return this;
  }

  public IncrementalScriptProcess exec() throws IOException, FileNotInSourcePathException {

    if (filePath.isPresent() && !isFileInSourcePath()) {
      throw new FileNotInSourcePathException(filePath.get(), incrementalScriptBean.getSourcePaths());
    }

    if (isRunning()) {
      console.info(String.format("Already running: %s", incrementalScriptBean.toString()));
      return restart();
    }

    return start();
  }

  private IncrementalScriptProcess start() throws IOException {
    process = Runtime.getRuntime().exec(incrementalScriptBean.getSourceCodeOfScript().split("[\\s]+"), null, new File(workingDirectory));
    errorGobbler = new StreamGobbler(process.getErrorStream(), console, ERROR);
    outputGobbler = new StreamGobbler(process.getInputStream(), console, INFO);
    errorGobbler.start();
    outputGobbler.start();

    stopwatch = new Stopwatch().start();
    runningProcesses.add(this);
    console.info(String.format("Started: %s", incrementalScriptBean.toString()));
    return this;
  }

  public IncrementalScriptProcess restart() throws IOException, FileNotInSourcePathException {
    console.info(String.format("Restart: %s", incrementalScriptBean.toString()));
    restarting = true;
    final IncrementalScriptProcess incrementalScriptProcess = kill().start();
    restarting = false;
    return incrementalScriptProcess;
  }

  public IncrementalScriptProcess kill() {
    errorGobbler.interrupt();
    outputGobbler.interrupt();
    getProcess().destroy();
    runningProcesses.remove(this);
    return this;
  }

  public int waitFor() throws InterruptedException {
    int exitCode = -1;
    do {
      try {
        exitCode = this.process.waitFor();
        if (stopwatch.isRunning()) {
          console.info(String.format("Stopped execution with exit code %d after %d ms of %s",
              exitCode,
              stopwatch.stop().elapsed(TimeUnit.MILLISECONDS),
              incrementalScriptBean.toString()));
        }
        runningProcesses.remove(this);
        return exitCode;
      } catch (InterruptedException e) {
        if (!restarting) {
          throw e;
        }
      }
    } while (restarting);

    // to make compiler happy
    return exitCode;
  }

  public boolean isRunning() {
    return runningProcesses.contains(this);
  }

  private boolean isFileInSourcePath() {
    final Collection<String> sourcePaths = incrementalScriptBean.getSourcePaths();
    return from(sourcePaths).anyMatch(new Predicate<String>() {
      @Override
      public boolean apply(String sourcePath) {
        return filePath.get().startsWith(sourcePath);
      }
    });
  }

  public Process getProcess() {
    return process;
  }

  public IncrementalScriptBean getIncrementalScriptBean() {
    return incrementalScriptBean;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IncrementalScriptProcess that = (IncrementalScriptProcess) o;

    if (incrementalScriptBean != null ? !incrementalScriptBean.equals(that.incrementalScriptBean) : that.incrementalScriptBean != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return incrementalScriptBean != null ? incrementalScriptBean.hashCode() : 0;
  }
}
