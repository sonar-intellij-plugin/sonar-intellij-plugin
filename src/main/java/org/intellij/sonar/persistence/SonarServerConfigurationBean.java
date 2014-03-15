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
  public void loadPassword() {
    this.password = PasswordManager.loadPassword(this.name);
  }

  @Transient
  public void storePassword() {
    PasswordManager.storePassword(this.name, this.password);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServerConfigurationBean bean = (SonarServerConfigurationBean) o;

    if (name != null ? !name.equals(bean.name) : bean.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
