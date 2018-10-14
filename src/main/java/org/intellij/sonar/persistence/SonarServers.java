package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
  name = "sonarServers",
  storages = {
    @Storage("sonarSettings.xml")
  }
)
public class SonarServers implements PersistentStateComponent<SonarServers> {

  public static final String NO_SONAR = "<NO SONAR>";
  public static final String PROJECT = "<PROJECT>";
  public Collection<SonarServerConfig> beans = new ArrayList<SonarServerConfig>();

  @NotNull
  public static SonarServers getInstance() {
    return ServiceManager.getService(SonarServers.class);
  }

  public static void add(final SonarServerConfig newSonarServerConfigBean) {
    final Collection<SonarServerConfig> sonarServerConfigBeans = SonarServers.getInstance().getState().beans;
    final boolean alreadyExists = sonarServerConfigBeans.stream()
        .anyMatch(sonarServerConfigurationBean -> sonarServerConfigurationBean.equals(newSonarServerConfigBean));
    if (alreadyExists) {
      throw new IllegalArgumentException("already exists");
    } else {
      sonarServerConfigBeans.add(newSonarServerConfigBean);
      if (newSonarServerConfigBean.isPasswordChanged()) {
        newSonarServerConfigBean.storePassword();
      }
      newSonarServerConfigBean.clearPassword();
    }
  }

  public static void remove(@NotNull final String sonarServerName) {
    final Optional<SonarServerConfig> bean = get(sonarServerName);
    Preconditions.checkArgument(bean.isPresent());
    final List<SonarServerConfig> newBeans = getAll().get().stream()
        .filter(sonarServerConfigurationBean -> !bean.get().equals(sonarServerConfigurationBean))
        .collect(Collectors.toList());
    getInstance().beans = new LinkedList<SonarServerConfig>(newBeans);
  }

  public static Optional<SonarServerConfig> get(@NotNull final String sonarServerName) {
    Optional<SonarServerConfig> bean = Optional.empty();
    final Optional<Collection<SonarServerConfig>> allBeans = getAll();
    if (allBeans.isPresent()) {
      bean = allBeans.get().stream().filter(sonarServerConfigBean -> sonarServerName.equals(sonarServerConfigBean.getName())).findFirst();
    }
    return bean;
  }

  public static Optional<Collection<SonarServerConfig>> getAll() {
    return Optional.ofNullable(SonarServers.getInstance().getState().beans);
  }

  @NotNull
  @Override
  public SonarServers getState() {
    return this;
  }

  @Override
  public void loadState(SonarServers state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarServers that = (SonarServers) o;
    if (beans != null
      ? !beans.equals(that.beans)
      : that.beans != null)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return beans != null
      ? beans.hashCode()
      : 0;
  }
}
