package org.intellij.sonar.configuration;

import com.intellij.ide.passwordSafe.PasswordSafeException;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.util.ArrayList;


public final class ProjectSettingsConfigurableBeanConverter {

    private ProjectSettingsConfigurableBeanConverter() {}

    public static ProjectSettingsBean convertToBean(ProjectSettingsConfigurable projectSettingsConfigurable) {

        ProjectSettingsBean projectSettingsBean = new ProjectSettingsBean();
        projectSettingsBean.sonarServerHostUrl = projectSettingsConfigurable.getSonarServerTextField().getText();
        projectSettingsBean.useAnonymous = projectSettingsConfigurable.getUseAnonymousCheckBox().isSelected();
        projectSettingsBean.shareConfiguration = projectSettingsConfigurable.getShareConfigurationMakesVisibleCheckBox().isSelected();
        projectSettingsBean.user = projectSettingsConfigurable.getUserTextField().getText();
        projectSettingsBean.password = String.valueOf(projectSettingsConfigurable.getPasswordField().getPassword());

        convertResourcesListToBean(projectSettingsConfigurable, projectSettingsBean);

        return projectSettingsBean;
    }

    private static void convertResourcesListToBean(ProjectSettingsConfigurable projectSettingsConfigurable, ProjectSettingsBean projectSettingsBean) {
        ListModel model = projectSettingsConfigurable.getResourcesList().getModel();
        projectSettingsBean.resources = new ArrayList<String>(model.getSize());
        for (int i=0; i < model.getSize(); i++) {
            projectSettingsBean.resources.add((String) model.getElementAt(i));
        }
    }

    public static ProjectSettingsConfigurable convertFromBean(ProjectSettingsBean projectSettingsBean, ProjectSettingsConfigurable projectSettingsConfigurable) {
        if (null == projectSettingsBean) return null;

        projectSettingsConfigurable.getSonarServerTextField().setText(projectSettingsBean.sonarServerHostUrl);
        projectSettingsConfigurable.getUseAnonymousCheckBox().setSelected(projectSettingsBean.useAnonymous);
        projectSettingsConfigurable.getShareConfigurationMakesVisibleCheckBox().setSelected(projectSettingsBean.shareConfiguration);
        projectSettingsConfigurable.getUserTextField().setText(projectSettingsBean.user);
        projectSettingsConfigurable.getPasswordField().setText(projectSettingsBean.password);
        projectSettingsConfigurable.getResourcesList().setListData(projectSettingsBean.resources.toArray());

        return projectSettingsConfigurable;

    }
}
