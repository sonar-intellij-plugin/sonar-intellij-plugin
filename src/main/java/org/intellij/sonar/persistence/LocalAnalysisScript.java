package org.intellij.sonar.persistence;

import com.google.common.base.Objects;

public class LocalAnalysisScript {

  private String name;
  private String sourceCode;
  private String pathToSonarReport;

  public LocalAnalysisScript() {
  }

  public LocalAnalysisScript(String name,String sourceCode,String pathToSonarReport) {
    this.name = name;
    this.sourceCode = sourceCode;
    this.pathToSonarReport = pathToSonarReport;
  }

  public static LocalAnalysisScript of(String name,String sourceCode,String pathToSonarReport) {
    return new LocalAnalysisScript(name,sourceCode,pathToSonarReport);
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
    return Objects.equal(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public String toString() {
    return "LocalAnalysisScript{" +
            "name='" + name + '\'' +
            ", sourceCode='" + sourceCode + '\'' +
            ", pathToSonarReport='" + pathToSonarReport + '\'' +
            '}';
  }
}
