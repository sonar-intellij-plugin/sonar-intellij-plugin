package org.intellij.sonar;

import org.apache.commons.lang.StringUtils;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SonarSettingsBean that = (SonarSettingsBean) o;

        if (null != host ? !host.equals(that.host) : null != that.host) return false;
        if (null != password ? !password.equals(that.password) : null != that.password) return false;
        if (null != resource ? !resource.equals(that.resource) : null != that.resource) return false;
        if (null != user ? !user.equals(that.user) : null != that.user) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = null != host ? host.hashCode() : 0;
        result = 31 * result + (null != user ? user.hashCode() : 0);
        result = 31 * result + (null != password ? password.hashCode() : 0);
        result = 31 * result + (null != resource ? resource.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return host + user + password + resource;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(host) && StringUtils.isBlank(user) && StringUtils.isBlank(password) && StringUtils.isBlank(resource);
    }
}
