package org.mayevskiy.intellij.sonar;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
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
    public Project project;

    public SonarRulesProvider() {
        this.sonarRules = new LinkedList<>();
    }

    public SonarRulesProvider(Project project) {
        this();
        this.project = project;
    }

    @Nullable
    @Override
    public SonarRulesProvider getState() {
        return this;
    }

    @Override
    public void loadState(SonarRulesProvider state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
