package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;

public class IncrementalScriptBean {

  private Collection<String> sourcePaths = new ArrayList<String>();
  private String sourceCodeOfScript;
  private String pathToSonarReport;

  public static IncrementalScriptBean of(Collection<String> sourcePaths, String sourceCodeOfScript, String pathToSonarReport) {
    IncrementalScriptBean bean = new IncrementalScriptBean();
    bean.sourcePaths = sourcePaths;
    bean.sourceCodeOfScript = sourceCodeOfScript;
    bean.pathToSonarReport = pathToSonarReport;
    return bean;
  }

  public void setSourcePaths(Collection<String> sourcePaths) {
    this.sourcePaths = sourcePaths;
  }

  public void setSourceCodeOfScript(String sourceCodeOfScript) {
    this.sourceCodeOfScript = sourceCodeOfScript;
  }

  public void setPathToSonarReport(String pathToSonarReport) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IncrementalScriptBean that = (IncrementalScriptBean) o;

    if (pathToSonarReport != null ? !pathToSonarReport.equals(that.pathToSonarReport) : that.pathToSonarReport != null)
      return false;
    if (sourceCodeOfScript != null ? !sourceCodeOfScript.equals(that.sourceCodeOfScript) : that.sourceCodeOfScript != null)
      return false;
    if (sourcePaths != null ? !sourcePaths.equals(that.sourcePaths) : that.sourcePaths != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sourcePaths != null ? sourcePaths.hashCode() : 0;
    result = 31 * result + (sourceCodeOfScript != null ? sourceCodeOfScript.hashCode() : 0);
    result = 31 * result + (pathToSonarReport != null ? pathToSonarReport.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "IncrementalScriptBean{" +
        "sourcePaths=" + sourcePaths +
        ", sourceCodeOfScript='" + sourceCodeOfScript + '\'' +
        ", pathToSonarReport='" + pathToSonarReport + '\'' +
        '}';
  }
}
