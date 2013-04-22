package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Violation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.04.13
 * Time: 13:40
 */
@State(
        name = "SonarViolations",
        storages = {
                @Storage(id = "other", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarViolationsComponent implements ProjectComponent, PersistentStateComponent<Map<String, Collection<Violation>>> {

    private Map<String, Collection<Violation>> sonarViolations;

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    @Override
    public void initComponent() {
    }

    @Override
    public void disposeComponent() {
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Sonar violations";
    }

    @Nullable
    @Override
    public Map<String, Collection<Violation>> getState() {
        return sonarViolations;
    }

    @Override
    public void loadState(Map<String, Collection<Violation>> state) {
        this.sonarViolations = state;
    }

    public void syncWithSonar(final Project project) {
        clearState();

        Collection<SonarSettingsBean> allSonarSettingsBeans = getSonarSettingsBeans(project);

        final Map<String, Collection<Violation>> violationsMap = getViolationsFromSonar(allSonarSettingsBeans);

        loadState(violationsMap);
    }

    private Map<String, Collection<Violation>> getViolationsFromSonar(Collection<SonarSettingsBean> allSonarSettingsBeans) {
        Map<String, Collection<Violation>> violationsMap = getState();
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

    private Collection<SonarSettingsBean> getSonarSettingsBeans(final Project project) {
        final Map<String, SonarSettingsBean> sonarSettingsMap = new HashMap<>();
        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile fileOrDir) {
                final AccessToken readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

                try {
                    SonarSettingsBean sonarSettingsBean = SonarSettingsUtils.getSonarSettingsBeanForFile(fileOrDir, project);
                    if (null != sonarSettingsBean) {
                        if (!sonarSettingsMap.containsKey(sonarSettingsBean.toString())) {
                            sonarSettingsMap.put(sonarSettingsBean.toString(), sonarSettingsBean);
                        }
                    }
                } finally {
                    readAccessToken.finish();
                }
                return true;
            }
        });
        Collection<SonarSettingsBean> sonarSettingsBeansOfAllModules = sonarSettingsMap.values();
        SonarSettingsBean sonarSettingsBeanOfProject = project.getComponent(SonarProjectSettingsComponent.class).getState();

        Collection<SonarSettingsBean> allSonarSettingsBeans = new LinkedList<>();
        allSonarSettingsBeans.addAll(sonarSettingsBeansOfAllModules);
        allSonarSettingsBeans.add(sonarSettingsBeanOfProject);
        return allSonarSettingsBeans;
    }

    private void clearState() {
        Map<String, Collection<Violation>> state = getState();
        if (null != state) {
            state.clear();
        }
    }
}
