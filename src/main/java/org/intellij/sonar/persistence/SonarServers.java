package org.intellij.sonar.persistence;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.*;
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
    public Collection<SonarServerConfig> beans = new ArrayList<SonarServerConfig>();

    @NotNull
    public static SonarServers getInstance() {
        return ServiceManager.getService(SonarServers.class);
    }

    public static void add(final SonarServerConfig newSonarServerConfigBean) {
        final Collection<SonarServerConfig> sonarServerConfigBeans = SonarServers.getInstance().getState().beans;
        final boolean alreadyExists = FluentIterable.from(sonarServerConfigBeans).anyMatch(new Predicate<SonarServerConfig>() {
            @Override
            public boolean apply(SonarServerConfig sonarServerConfigurationBean) {
                return sonarServerConfigurationBean.equals(newSonarServerConfigBean);
            }
        });
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
        final ImmutableList<SonarServerConfig> newBeans = FluentIterable.from(getAll().get()).filter(new Predicate<SonarServerConfig>() {
            @Override
            public boolean apply(SonarServerConfig sonarServerConfigurationBean) {
                return !bean.get().equals(sonarServerConfigurationBean);
            }
        }).toList();
        getInstance().beans = new LinkedList<SonarServerConfig>(newBeans);
    }

    public static Optional<SonarServerConfig> get(@NotNull final String sonarServerName) {
        Optional<SonarServerConfig> bean = Optional.absent();
        final Optional<Collection<SonarServerConfig>> allBeans = getAll();
        if (allBeans.isPresent()) {
            bean = FluentIterable.from(allBeans.get()).firstMatch(new Predicate<SonarServerConfig>() {
                @Override
                public boolean apply(SonarServerConfig sonarServerConfigBean) {
                    return sonarServerName.equals(sonarServerConfigBean.getName());
                }
            });
        }
        return bean;
    }

    public static Optional<Collection<SonarServerConfig>> getAll() {
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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SonarServers that = (SonarServers) o;

        if (beans != null ? !beans.equals(that.beans) : that.beans != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return beans != null ? beans.hashCode() : 0;
    }
}
