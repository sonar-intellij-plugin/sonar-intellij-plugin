package org.intellij.sonar.persistence;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@State(
  name = "localAnalysisScripts",
  storages = {
    @Storage(id = "localAnalysisScripts", file = StoragePathMacros.APP_CONFIG+"/sonarSettings.xml")
  }
)
public class LocalAnalysisScripts implements PersistentStateComponent<LocalAnalysisScripts> {

  public static final String NO_LOCAL_ANALYSIS = "<NO LOCAL ANALYSIS>";
  public static final String PROJECT = "<PROJECT>";
  public Collection<LocalAnalysisScript> beans = new ArrayList<LocalAnalysisScript>();

  @NotNull
  public static LocalAnalysisScripts getInstance() {
    return ServiceManager.getService(LocalAnalysisScripts.class);
  }

  public static void add(final LocalAnalysisScript newLocalAnalysisScript) {
    final Collection<LocalAnalysisScript> localAnalysisScripts = LocalAnalysisScripts.getInstance().getState().beans;
    final boolean alreadyExists = FluentIterable.from(localAnalysisScripts).anyMatch(
      new Predicate<LocalAnalysisScript>() {
        @Override
        public boolean apply(LocalAnalysisScript localAnalysisScript) {
          return localAnalysisScript.equals(newLocalAnalysisScript);
        }
      }
    );
    if (alreadyExists) {
      throw new IllegalArgumentException("already exists");
    } else {
      localAnalysisScripts.add(newLocalAnalysisScript);
    }
  }

  public static void remove(@NotNull final String name) {
    final Optional<LocalAnalysisScript> bean = get(name);
    Preconditions.checkArgument(bean.isPresent());
    final ImmutableList<LocalAnalysisScript> newBeans = FluentIterable.from(getAll()).filter(
      new Predicate<LocalAnalysisScript>() {
        @Override
        public boolean apply(LocalAnalysisScript localAnalysisScript) {
          return !bean.get().equals(localAnalysisScript);
        }
      }
    ).toList();
    getInstance().beans = new ArrayList<LocalAnalysisScript>(newBeans);
  }

  public static Optional<LocalAnalysisScript> get(@NotNull final String name) {
    final Collection<LocalAnalysisScript> allBeans = getAll();
    return FluentIterable.from(allBeans).firstMatch(
      new Predicate<LocalAnalysisScript>() {
        @Override
        public boolean apply(LocalAnalysisScript localAnalysisScript) {
          return name.equals(localAnalysisScript.getName());
        }
      }
    );
  }

  public static Collection<LocalAnalysisScript> getAll() {
    return LocalAnalysisScripts.getInstance().getState().beans;
  }

  @NotNull
  @Override
  public LocalAnalysisScripts getState() {
    return this;
  }

  @Override
  public void loadState(LocalAnalysisScripts state) {
    XmlSerializerUtil.copyBean(state,this);
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocalAnalysisScripts that = (LocalAnalysisScripts) o;
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
