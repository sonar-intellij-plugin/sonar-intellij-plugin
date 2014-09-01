package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

@State(
    name = "sonarServers",
    storages = {
        @Storage(id = "sonarServers", file = StoragePathMacros.APP_CONFIG + "/sonarSettings.xml")
    }
)

public class SonarServers implements PersistentStateComponent<SonarServers> {

  public static final String NO_SONAR = "<NO SONAR>";
  public static final String PROJECT = "<PROJECT>";
  public Collection<SonarServerConfigurationBean> beans = new ArrayList<SonarServerConfigurationBean>();

  @NotNull
  public static SonarServers getInstance() {
    return ServiceManager.getService(SonarServers.class);
  }

  public static void add(final SonarServerConfigurationBean newSonarServerConfigurationBean) {
    final Collection<SonarServerConfigurationBean> sonarServerConfigurationBeans = SonarServers.getInstance().getState().beans;
    final boolean alreadyExists = FluentIterable.from(sonarServerConfigurationBeans).anyMatch(new Predicate<SonarServerConfigurationBean>() {
      @Override
      public boolean apply(SonarServerConfigurationBean sonarServerConfigurationBean) {
        return sonarServerConfigurationBean.equals(newSonarServerConfigurationBean);
      }
    });
    if (alreadyExists) {
      throw new IllegalArgumentException("already exists");
    } else {
      sonarServerConfigurationBeans.add(newSonarServerConfigurationBean);
      if (!StringUtil.isEmptyOrSpaces(newSonarServerConfigurationBean.getPassword())) {
        newSonarServerConfigurationBean.storePassword();
        newSonarServerConfigurationBean.clearPassword();
      }
    }
  }

  public static void remove(@NotNull final String sonarServerName) {
    final Optional<SonarServerConfigurationBean> bean = get(sonarServerName);
    Preconditions.checkArgument(bean.isPresent());
    final ImmutableList<SonarServerConfigurationBean> newBeans = FluentIterable.from(getAll().get()).filter(new Predicate<SonarServerConfigurationBean>() {
      @Override
      public boolean apply(SonarServerConfigurationBean sonarServerConfigurationBean) {
        return !bean.get().equals(sonarServerConfigurationBean);
      }
    }).toList();
    getInstance().beans = new LinkedList<SonarServerConfigurationBean>(newBeans);
  }

  public static Optional<SonarServerConfigurationBean> get(@NotNull final String sonarServerName) {
    Optional<SonarServerConfigurationBean> bean = Optional.absent();
    final Optional<Collection<SonarServerConfigurationBean>> allBeans = getAll();
    if (allBeans.isPresent()) {
      bean = FluentIterable.from(allBeans.get()).firstMatch(new Predicate<SonarServerConfigurationBean>() {
        @Override
        public boolean apply(SonarServerConfigurationBean sonarServerConfigurationBean) {
          return sonarServerName.equals(sonarServerConfigurationBean.getName());
        }
      });
    }
    return bean;
  }

  public static Optional<Collection<SonarServerConfigurationBean>> getAll() {
    return Optional.fromNullable(SonarServers.getInstance().getState().beans);
  }

  @NotNull
  @Override
  public SonarServers getState() {
    return this;
  }

  @Override
  public void loadState(SonarServers state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SonarServers that = (SonarServers) o;

    if (beans != null ? !beans.equals(that.beans) : that.beans != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return beans != null ? beans.hashCode() : 0;
  }
}
