package org.mayevskiy.intellij.sonar.bean;

public class SonarSettingsFactory {

    public static SonarSettingsBean getInstance() {
        return new SonarSettingsBean();
    }
}
