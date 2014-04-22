package org.intellij.sonar.configuration.check;

import com.google.common.base.Optional;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.sonar.persistence.IncrementalScriptBean;

import java.util.List;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class FileExistenceCheck implements Runnable, ConfigurationCheck {
  private final String path;
  private boolean fileExists;

  public FileExistenceCheck(String path) {
    this.path = path;
  }

  public static String checkSourceDirectoryPaths(Project project, List<IncrementalScriptBean> incrementalScriptBeans) {
    StringBuilder sb = new StringBuilder();
    for (IncrementalScriptBean incrementalScriptBean : incrementalScriptBeans) {
      for (String sourceDirectoryPath : incrementalScriptBean.getSourcePaths()) {
        FileExistenceCheck fileExistenceCheck = new FileExistenceCheck(sourceDirectoryPath);
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            fileExistenceCheck,
            "Testing File Existence", true, project
        );
        sb.append(fileExistenceCheck.getMessage());
      }
    }
    return sb.toString();
  }

  public void run() {
    final Optional<VirtualFile> virtualFile = Optional.fromNullable(
        LocalFileSystem.getInstance().findFileByPath(path)
    );
    fileExists = virtualFile.isPresent() && virtualFile.get().exists();
  }

  @Override
  public boolean isOk() {
    return fileExists;
  }

  @Override
  public String getMessage() {
    if (fileExists) {
      return okMessage(String.format("%s exists.\n", path));
    } else {
      return errorMessage(String.format("%s does not exist.\n", path));
    }
  }
}
