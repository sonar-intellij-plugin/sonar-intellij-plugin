package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "sonarConsoleSettings",
  storages = {
    @Storage("/sonarSettings.xml")
  }
)
public class SonarConsoleSettings implements PersistentStateComponent<SonarConsoleSettings> {

  private boolean showSonarConsoleOnAnalysis = true;

  public static SonarConsoleSettings getInstance() {
    return ServiceManager.getService(SonarConsoleSettings.class);
  }

  @NotNull
  public static SonarConsoleSettings of(boolean showSonarConsoleOnAnalysis) {
    final SonarConsoleSettings sonarConsoleSettings = new SonarConsoleSettings();
    sonarConsoleSettings.setShowSonarConsoleOnAnalysis(showSonarConsoleOnAnalysis);
    return sonarConsoleSettings;
  }

  @Nullable
  @Override
  public SonarConsoleSettings getState() {
    return this;
  }

  @Override
  public void loadState(SonarConsoleSettings state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  public boolean isShowSonarConsoleOnAnalysis() {
    return showSonarConsoleOnAnalysis;
  }

  public void setShowSonarConsoleOnAnalysis(boolean showSonarConsoleOnAnalysis) {
    this.showSonarConsoleOnAnalysis = showSonarConsoleOnAnalysis;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarConsoleSettings that = (SonarConsoleSettings) o;
    if (showSonarConsoleOnAnalysis != that.showSonarConsoleOnAnalysis)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return (showSonarConsoleOnAnalysis
      ? 1
      : 0);
  }
}
