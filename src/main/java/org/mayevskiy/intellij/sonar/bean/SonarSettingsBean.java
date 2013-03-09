package org.mayevskiy.intellij.sonar.bean;

public class SonarSettingsBean {
    public String host;
    public String user;
    public String password;
    public String resource;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SonarSettingsBean that = (SonarSettingsBean) o;

        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        return result;
    }
}
