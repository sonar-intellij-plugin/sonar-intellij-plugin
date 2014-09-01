package org.intellij.sonar.persistence;

public class LocalAnalysisScript {
  private String name;
  private String sourceCode;
  private String pathToSonarReport;

  public LocalAnalysisScript() {
  }

  public LocalAnalysisScript(String name, String sourceCode, String pathToSonarReport) {
    this.name = name;
    this.sourceCode = sourceCode;
    this.pathToSonarReport = pathToSonarReport;
  }

  public static LocalAnalysisScript of(String name, String sourceCode, String pathToSonarReport) {
    return new LocalAnalysisScript(name, sourceCode, pathToSonarReport);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSourceCode() {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode) {
    this.sourceCode = sourceCode;
  }

  public String getPathToSonarReport() {
    return pathToSonarReport;
  }

  public void setPathToSonarReport(String pathToSonarReport) {
    this.pathToSonarReport = pathToSonarReport;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LocalAnalysisScript that = (LocalAnalysisScript) o;

    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
