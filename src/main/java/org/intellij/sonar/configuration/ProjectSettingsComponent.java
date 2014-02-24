package org.intellij.sonar.configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "project-settings-component",
        storages = {
                @Storage(id = "project-settings-component-id", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/sonar-intellij-plugin.xml")
        }
)

public class ProjectSettingsComponent implements PersistentStateComponent<ProjectSettingsBean>, ProjectComponent {
    protected ProjectSettingsBean projectSettingsBean;

    private Project project;

    public ProjectSettingsComponent(Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public ProjectSettingsBean getState() {
        if (null != projectSettingsBean) {
            projectSettingsBean.password = PasswordManager.loadPassword(project, projectSettingsBean);
        }
        return projectSettingsBean;
    }

    @Override
    public void loadState(ProjectSettingsBean projectSettingsBean) {
        this.projectSettingsBean = projectSettingsBean;
    }

    @Override
    public void projectOpened() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void projectClosed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "SonarQube";
    }
}
