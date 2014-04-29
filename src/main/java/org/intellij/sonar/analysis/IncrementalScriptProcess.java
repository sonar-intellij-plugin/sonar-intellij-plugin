package org.intellij.sonar.analysis;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.console.StreamGobbler;
import org.intellij.sonar.persistence.IncrementalScriptBean;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.FluentIterable.from;
import static org.intellij.sonar.console.ConsoleLogLevel.ERROR;
import static org.intellij.sonar.console.ConsoleLogLevel.INFO;

public class IncrementalScriptProcess {

  private final static Logger LOG = Logger.getInstance(IncrementalScriptProcess.class);

  private final static ConcurrentHashMap<IncrementalScriptBean, Process> runningProcesses = new ConcurrentHashMap<IncrementalScriptBean, Process>();
  private final static ConcurrentHashMap<Integer, IncrementalScriptProcess> runningIncrementalScriptProcesses = new ConcurrentHashMap<Integer, IncrementalScriptProcess>();

  private final IncrementalScriptBean incrementalScriptBean;
  private final String workingDirectory;
  private final SonarConsole console;
  private Stopwatch stopwatch;
  private Process process;
  private Optional<String> filePath;
  private boolean restarted = false;

  public static IncrementalScriptProcess of(IncrementalScriptBean incrementalScriptBean, String workingDirectory, Project project) {
    final IncrementalScriptProcess incrementalScriptProcess = new IncrementalScriptProcess(incrementalScriptBean, workingDirectory, project);
    final int hashCode = incrementalScriptProcess.hashCode();
    runningIncrementalScriptProcesses.putIfAbsent(hashCode, incrementalScriptProcess);
    return runningIncrementalScriptProcesses.get(hashCode);
  }

  private IncrementalScriptProcess(IncrementalScriptBean incrementalScriptBean, String workingDirectory, Project project) {
    this.incrementalScriptBean = incrementalScriptBean;
    this.workingDirectory = workingDirectory;
    this.console = SonarConsole.get(project);
    this.filePath = Optional.absent();
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
      final Process runningProcess = runningProcesses.get(incrementalScriptBean);
      console.info(String.format("Already running: %s", incrementalScriptBean.toString()));
      return restart(runningProcess);
    }

    return start();
  }

  private IncrementalScriptProcess start() throws IOException {
    process = Runtime.getRuntime().exec(incrementalScriptBean.getSourceCodeOfScript(), null, new File(workingDirectory));
    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), console, ERROR);
    StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), console, INFO);
    errorGobbler.start();
    outputGobbler.start();
    runningProcesses.put(incrementalScriptBean, process);
    stopwatch = new Stopwatch().start();
    console.info(String.format("Started: %s", incrementalScriptBean.toString()));
    return this;
  }

  private IncrementalScriptProcess restart(Process runningProcess) throws IOException, FileNotInSourcePathException {
    restarted = true;
    console.info(String.format("Restarting: %s", incrementalScriptBean.toString()));
    stop(runningProcess);
    return this.exec();
  }

  private void stop(Process runningProcess) {
    runningProcess.destroy();
    runningProcesses.remove(incrementalScriptBean);
  }

  public int waitFor() throws InterruptedException {
    try {
      final int exitCode = this.process.waitFor();
      if (!restarted && 0 != exitCode) {
        LOG.error(String.format("Cannot execute. Exit code: %d\nScript: %s", exitCode, incrementalScriptBean.getSourceCodeOfScript()));
      }
      return exitCode;
    } catch (InterruptedException e) {
      if (!restarted) {
        throw e;
      } else {
        return 0;
      }
    } finally {
      console.info(String.format("Stopped execution after %d ms of %s", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS), incrementalScriptBean.toString()));
      runningProcesses.remove(incrementalScriptBean);
      if (!restarted) runningIncrementalScriptProcesses.remove(this.hashCode());
    }
  }

  public boolean isRunning() {
    final Optional<Process> runningProcess = fromNullable(runningProcesses.get(incrementalScriptBean));
    return runningProcess.isPresent();
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
