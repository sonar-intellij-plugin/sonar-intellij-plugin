package org.intellij.sonar.analysis;

import java.util.Collection;

public class FileNotInSourcePathException extends Exception{

  private static final long serialVersionUID = -3707124641738802047L;

  private final String filePath;
  private final Collection<String> sourcePaths;

  public FileNotInSourcePathException(String filePath, Collection<String> sourcePaths) {
    this.filePath = filePath;
    this.sourcePaths = sourcePaths;
  }

  @Override
  public String toString() {
    return "FileNotInSourcePathException{" +
        "filePath='" + filePath + '\'' +
        ", sourcePaths=" + sourcePaths +
        '}';
  }
}
