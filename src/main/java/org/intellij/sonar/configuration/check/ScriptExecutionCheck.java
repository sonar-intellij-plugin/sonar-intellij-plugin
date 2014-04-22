package org.intellij.sonar.configuration.check;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.intellij.sonar.persistence.IncrementalScriptBean;

import java.io.File;
import java.util.List;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class ScriptExecutionCheck implements Runnable, ConfigurationCheck {
  public static final int MAX_EXECUTION_TIME = 3000;
  private final IncrementalScriptBean myScript;
  private final File workingDirectory;
  private boolean canBeExecuted = false;
  private String errorMessage;

  public ScriptExecutionCheck(IncrementalScriptBean myScript, File workingDirectory) {
    this.myScript = myScript;
    this.workingDirectory = workingDirectory;
  }

  public static String checkScriptsExecution(Project project, List<IncrementalScriptBean> incrementalScriptBeans) {
    StringBuilder sb = new StringBuilder();
    for (IncrementalScriptBean incrementalScriptBean : incrementalScriptBeans) {
      ScriptExecutionCheck scriptExecutionCheck = new ScriptExecutionCheck(
          incrementalScriptBean, new File(project.getBaseDir().getPath()));
      ProgressManager.getInstance().runProcessWithProgressSynchronously(
          scriptExecutionCheck,
          "Testing Script Execution", true, project
      );
      sb.append(scriptExecutionCheck.getMessage());
    }

    return sb.toString();
  }

  @Override
  public void run() {
    final Process process;
    try {
      process = Runtime.getRuntime().exec(myScript.getSourceCodeOfScript(), null, workingDirectory);
      Thread.sleep(MAX_EXECUTION_TIME);
      try {
        final int exitCode = process.exitValue();
        canBeExecuted = (exitCode == 0);
        if (!canBeExecuted) {
          errorMessage = String.format("Terminated with exit code %d", exitCode);
        }
      } catch (IllegalThreadStateException e) {
        canBeExecuted = true;
        process.destroy();
      }
    } catch (Exception e) {
      errorMessage = String.format("%s",e.getMessage());
    }


  }

  @Override
  public boolean isOk() {
    return canBeExecuted;
  }

  @Override
  public String getMessage() {
    if (canBeExecuted) {
      return okMessage(String.format("Can execute %s\n", myScript.getSourceCodeOfScript()));
    } else {
      return errorMessage(String.format("Cannot execute %s\n\nRoot cause:\n\n%s\n", myScript.getSourceCodeOfScript(), errorMessage));
    }
  }
}
