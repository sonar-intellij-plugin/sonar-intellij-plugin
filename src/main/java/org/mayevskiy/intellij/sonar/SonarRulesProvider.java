package org.mayevskiy.intellij.sonar;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Rule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Author: Oleg Mayevskiy
 * Date: 01.05.13
 * Time: 12:42
 */
@State(
        name = "SonarRulesProvider",
        storages = {
                @Storage(id = "other", file = StoragePathMacros.PROJECT_FILE)
        }
)
public class SonarRulesProvider implements PersistentStateComponent<SonarRulesProvider> {

    public Collection<Rule> sonarRules;

    public SonarRulesProvider() {
        this.sonarRules = new LinkedList<>();
    }

    @SuppressWarnings("UnusedDeclaration")
    public SonarRulesProvider(Project project) {
        this();
    }

    @Nullable
    @Override
    public SonarRulesProvider getState() {
        // workaround NullPointerException of XmlSerializerUtil during serialisation
        // can be set to null because we don't need this info
        for (Rule sonarRule : sonarRules) {
            sonarRule.setParams(null);
        }
        return this;
    }

    @Override
    public void loadState(SonarRulesProvider state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void syncWithSonar(Project project) {
        int oldSize = sonarRules.size();
        clearState();
        Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
        SonarService sonarService = ServiceManager.getService(SonarService.class);
        if (null != allSonarSettingsBeans) {
            this.sonarRules.addAll(sonarService.getAllRules(allSonarSettingsBeans));
            int newSize = sonarRules.size();
            if (oldSize != newSize) {
                // show restart ide dialog
                final int ret = Messages.showOkCancelDialog("Detected new sonar rules. You have to restart IDE to reload the settings.\\nRestart?",
                        IdeBundle.message("title.restart.needed"), Messages.getQuestionIcon());
                if (ret == 0) {
                    if (ApplicationManager.getApplication().isRestartCapable()) {
                        ApplicationManager.getApplication().restart();
                    } else {
                        ApplicationManager.getApplication().exit();
                    }
                }
            }
        }
    }

    private void clearState() {
        if (null != this.sonarRules) {
            this.sonarRules.clear();
        }
    }
}
