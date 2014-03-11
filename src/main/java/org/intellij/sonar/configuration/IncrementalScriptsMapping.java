package org.intellij.sonar.configuration;

public class IncrementalScriptsMapping {

  private String scriptSource;
  private String executeOnPath;

  public String getScriptSource() {
    return scriptSource;
  }

  public void setScriptSource(String scriptSource) {
    this.scriptSource = scriptSource;
  }

  public String getExecuteOnPath() {
    return executeOnPath;
  }

  public void setExecuteOnPath(String executeOnPath) {
    this.executeOnPath = executeOnPath;
  }
}
