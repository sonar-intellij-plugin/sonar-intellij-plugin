package org.intellij.sonar.configuration.check;

import com.google.common.base.Optional;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import static org.intellij.sonar.util.MessagesUtil.errorMessage;
import static org.intellij.sonar.util.MessagesUtil.okMessage;

public class FileExistenceCheck implements Runnable, ConfigurationCheck {
  private final String path;
  private boolean fileExists;

  public FileExistenceCheck(String path) {
    this.path = path;
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
