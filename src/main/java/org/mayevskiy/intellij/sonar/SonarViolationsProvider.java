package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.sonar.wsclient.services.Violation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 13:40
 */
@State(
        name = "SonarViolationsProvider",
        storages = {
                @Storage(id = "other", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarViolationsProvider implements PersistentStateComponent<SonarViolationsProvider> {

    public Map<String, Collection<Violation>> mySonarViolations;

    public Project myProject;

    // fixes Could not save project: java.lang.InstantiationException
    public SonarViolationsProvider() {
        mySonarViolations = new HashMap<>();
    }

    public SonarViolationsProvider(Project project) {
        this();
        myProject = project;
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

    public void syncWithSonar(final Project project) {
        clearState();
        Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
        this.mySonarViolations = getViolationsFromSonar(allSonarSettingsBeans);
    }

    private Map<String, Collection<Violation>> getViolationsFromSonar(Collection<SonarSettingsBean> allSonarSettingsBeans) {
        Map<String, Collection<Violation>> violationsMap = this.mySonarViolations;
        if (null == violationsMap) {
            violationsMap = new ConcurrentHashMap<>();
        }
        for (SonarSettingsBean sonarSettingsBean : allSonarSettingsBeans) {
            List<Violation> violations = new SonarService().getViolations(sonarSettingsBean);
            if (null != violations) {
                for (Violation violation : violations) {
                    String resourceKey = violation.getResourceKey();
                    Collection<Violation> violationsOfFile = violationsMap.get(resourceKey);
                    if (null == violationsOfFile) {
                        violationsOfFile = new LinkedList<>();
                        violationsMap.put(resourceKey, violationsOfFile);
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
        }
        return violationsMap;
    }

    private void clearState() {
        if (null != this.mySonarViolations) {
            mySonarViolations.clear();
        }
    }
}
