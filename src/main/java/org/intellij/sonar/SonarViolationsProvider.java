package org.intellij.sonar;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.intellij.sonar.sonarserver.SonarService;
import org.jetbrains.annotations.NotNull;
import org.sonar.wsclient.services.Violation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 13:40
 */
@State(
    name = "SonarViolationsProvider",
    storages = {
        @Storage(id = "other", file = "$PROJECT_FILE$")
    }
)
public class SonarViolationsProvider implements PersistentStateComponent<SonarViolationsProvider> {

  public Map<String, Collection<Violation>> mySonarViolations;

  @SuppressWarnings("UnusedDeclaration")
  @Transient
  private Project project;

  // fixes Could not save project: java.lang.InstantiationException
  public SonarViolationsProvider() {
    this.mySonarViolations = new ConcurrentHashMap<String, Collection<Violation>>();
  }

  @SuppressWarnings("UnusedDeclaration")
  public SonarViolationsProvider(Project project) {
    this();
  }

  @NotNull
  @Override
  public SonarViolationsProvider getState() {
    return this;
  }

  @Override
  public void loadState(SonarViolationsProvider state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public int syncWithSonar(final Project project, @NotNull ProgressIndicator indicator) {
    clearState();
    Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    this.mySonarViolations = getViolationsFromSonar(allSonarSettingsBeans, indicator);
    return getMySonarViolations().size();
  }

  private Map<String, Collection<Violation>> getViolationsFromSonar(Collection<SonarSettingsBean> allSonarSettingsBeans, @NotNull ProgressIndicator indicator) {
    Map<String, Collection<Violation>> violationsMap = getMySonarViolations();
    if (null == violationsMap) {
      violationsMap = new ConcurrentHashMap<String, Collection<Violation>>();
    }
    SonarService sonarService = ServiceManager.getService(SonarService.class);
    for (SonarSettingsBean sonarSettingsBean : allSonarSettingsBeans) {
      indicator.checkCanceled();

      if (sonarSettingsBean.isEmpty()) {
        continue;
      }
      List<Violation> violations = sonarService.getViolations(sonarSettingsBean);
      for (Violation violation : violations) {
        indicator.checkCanceled();

        String resourceKey = violation.getResourceKey();
        Collection<Violation> violationsOfFile = null != resourceKey ? violationsMap.get(resourceKey) : null;
        if (null == violationsOfFile) {
          violationsOfFile = new LinkedList<Violation>();
          if (null != resourceKey) {
            violationsMap.put(resourceKey, violationsOfFile);
          }
        }

        boolean violationAlreadyExists = false;
        for (Violation alreadyExistingViolation : violationsOfFile) {
          if (SonarViolationUtils.isEqual(alreadyExistingViolation, violation)) {
            violationAlreadyExists = true;
            break;
          }
        }

        if (!violationAlreadyExists) {
          violationsOfFile.add(violation);
        }
      }
    }

    return violationsMap;
  }

  private Map<String, Collection<Violation>> getMySonarViolations() {
    if (null == mySonarViolations) {
      mySonarViolations = new ConcurrentHashMap<String, Collection<Violation>>();
    }
    return mySonarViolations;
  }

  private void clearState() {
    if (null != getMySonarViolations()) {
      getMySonarViolations().clear();
    }
  }
}
