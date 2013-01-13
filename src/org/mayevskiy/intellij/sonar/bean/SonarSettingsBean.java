package org.mayevskiy.intellij.sonar.bean;

public class SonarSettingsBean {
    public String host;
    public String user;
    public String password;
    public String resource;

    public SonarSettingsBean() {
    }

    public SonarSettingsBean(String host, String user, String password, String resource) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.resource = resource;
    }
}
