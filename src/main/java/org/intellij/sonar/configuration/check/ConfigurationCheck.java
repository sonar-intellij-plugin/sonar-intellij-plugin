package org.intellij.sonar.configuration.check;

public interface ConfigurationCheck {

  String getMessage();

  boolean isOk();
}
