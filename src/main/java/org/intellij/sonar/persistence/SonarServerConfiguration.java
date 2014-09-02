package org.intellij.sonar.persistence;

import com.intellij.util.xmlb.annotations.Transient;
import org.intellij.sonar.configuration.PasswordManager;

public class SonarServerConfiguration {
  private String name;
  private String hostUrl;
  private boolean anonymous;
  private String user;
  // avoid save password in plain text
  @Transient
  private String password;

  public static SonarServerConfiguration of(String hostUrl) {
    return SonarServerConfiguration.of(null, hostUrl, true, null);
  }

  public static SonarServerConfiguration of(String name, String hostUrl, boolean anonymous, String user) {
    SonarServerConfiguration bean = new SonarServerConfiguration();
    bean.name = name;
    bean.hostUrl = hostUrl;
    bean.anonymous = anonymous;
    bean.user = user;
    return bean;
  }

  public String getName() {
    return name;
  }

  public String getHostUrl() {
    return hostUrl;
  }

  public boolean isAnonymous() {
    return anonymous;
  }

  public String getUser() {
    return user;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setHostUrl(String hostUrl) {
    this.hostUrl = hostUrl;
  }

  public void setAnonymous(boolean anonymous) {
    this.anonymous = anonymous;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Transient
  public void clearPassword() {
    this.password = null;
  }

  @Transient
  public String loadPassword() {
    this.password = PasswordManager.loadPassword(this.name);
    return this.password;
  }

  @Transient
  public void storePassword() {
    PasswordManager.storePassword(this.name, this.password);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServerConfiguration that = (SonarServerConfiguration) o;

    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
