package org.intellij.sonar.util;

import org.intellij.sonar.persistence.SonarServers;
import org.jetbrains.annotations.NotNull;

public final class SonarServersUtil {

  private SonarServersUtil() {
  }

  @NotNull
  public static String withDefaultForProject(String serverName) {
    if (null == serverName) return SonarServers.NO_SONAR;
    return serverName;
  }

  @NotNull
  public static String withDefaultForModule(String serverName) {
    if (null == serverName) return SonarServers.PROJECT;
    return serverName;
  }
}
