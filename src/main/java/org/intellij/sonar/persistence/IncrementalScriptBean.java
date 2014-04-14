package org.intellij.sonar.persistence;

import java.util.Collection;

public class IncrementalScriptBean {

  private final Collection<String> sourcePaths;
  private final String sourceCodeOfScript;
  private final String pathToSonarReport;

  public IncrementalScriptBean(Collection<String> sourcePaths, String sourceCodeOfScript, String pathToSonarReport) {
    this.sourcePaths = sourcePaths;
    this.sourceCodeOfScript = sourceCodeOfScript;
    this.pathToSonarReport = pathToSonarReport;
  }

  public Collection<String> getSourcePaths() {
    return sourcePaths;
  }

  public String getSourceCodeOfScript() {
    return sourceCodeOfScript;
  }

  public String getPathToSonarReport() {
    return pathToSonarReport;
  }
}
