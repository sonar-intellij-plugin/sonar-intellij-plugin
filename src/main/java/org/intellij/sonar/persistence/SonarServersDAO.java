package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.intellij.openapi.util.text.StringUtil;

import java.util.Set;

public class SonarServersDAO {

  public static void add(final SonarServerConfigurationBean newSonarServerConfigurationBean) {
    final Set<SonarServerConfigurationBean> sonarServerConfigurationBeans = SonarServersApplicationComponent.getInstance().getState().beans;
    final boolean alreadyExists = FluentIterable.from(sonarServerConfigurationBeans).anyMatch(new Predicate<SonarServerConfigurationBean>() {
      @Override
      public boolean apply(SonarServerConfigurationBean sonarServerConfigurationBean) {
        return sonarServerConfigurationBean.name.equals(newSonarServerConfigurationBean.name);
      }
    });
    if (alreadyExists) {
      throw new IllegalArgumentException("already exists");
    } else {
      sonarServerConfigurationBeans.add(newSonarServerConfigurationBean);
      if (!StringUtil.isEmptyOrSpaces(newSonarServerConfigurationBean.password)) {
        newSonarServerConfigurationBean.storePassword();
        newSonarServerConfigurationBean.password=null;
      }
    }
  }

  public static Optional<Set<SonarServerConfigurationBean>> getAll() {
    return Optional.fromNullable(SonarServersApplicationComponent.getInstance().getState().beans);
  }
}
