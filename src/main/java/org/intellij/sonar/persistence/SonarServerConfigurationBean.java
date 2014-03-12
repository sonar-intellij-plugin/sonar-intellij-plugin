package org.intellij.sonar.persistence;

import com.intellij.util.xmlb.annotations.Transient;
import org.intellij.sonar.configuration.PasswordManager;

public class SonarServerConfigurationBean {
  public String name;
  public String hostUrl;
  public boolean anonymous;
  public String user;
  // don't save password in plain text
  @Transient
  public String password;

  @Transient
  public String loadPassword() {
    return PasswordManager.loadPassword(this.name);
  }

  @Transient
  public void storePassword() {
    PasswordManager.storePassword(this.name, this.password);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServerConfigurationBean that = (SonarServerConfigurationBean) o;

    if (anonymous != that.anonymous) return false;
    if (hostUrl != null ? !hostUrl.equals(that.hostUrl) : that.hostUrl != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (password != null ? !password.equals(that.password) : that.password != null) return false;
    if (user != null ? !user.equals(that.user) : that.user != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (hostUrl != null ? hostUrl.hashCode() : 0);
    result = 31 * result + (anonymous ? 1 : 0);
    result = 31 * result + (user != null ? user.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }
}
