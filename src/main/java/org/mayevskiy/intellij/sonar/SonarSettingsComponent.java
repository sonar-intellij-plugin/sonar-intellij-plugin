package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Oleg Mayevskiy
 * Date: 10.03.13
 * Time: 20:48
 */
public class SonarSettingsComponent implements PersistentStateComponent<SonarSettingsBean> {
    protected SonarSettingsBean sonarSettings;

    public static Collection<SonarSettingsBean> getSonarSettingsBeans(final Project project) {
        final Map<String, SonarSettingsBean> sonarSettingsMap = new ConcurrentHashMap<>();
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
        SonarSettingsBean sonarSettingsBeanOfProject = project.getComponent(SonarSettingsProjectComponent.class).getState();

        Collection<SonarSettingsBean> allSonarSettingsBeans = new LinkedList<>();
        allSonarSettingsBeans.addAll(sonarSettingsBeansOfAllModules);
        if (null != sonarSettingsBeanOfProject) {
            allSonarSettingsBeans.add(sonarSettingsBeanOfProject);
        }
        return allSonarSettingsBeans;
    }

    @Override
    public SonarSettingsBean getState() {
        return sonarSettings;
    }

    @Override
    public void loadState(SonarSettingsBean state) {
        this.sonarSettings = state;
    }
}
