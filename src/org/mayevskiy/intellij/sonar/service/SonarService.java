package org.mayevskiy.intellij.sonar.service;

import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import java.util.List;

public class SonarService {
    public boolean testConnection(SonarSettingsBean sonarSettingsBean) {

        Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
        List<Violation> violations = sonar.findAll(ViolationQuery.createForResource(sonarSettingsBean.resource));
        for (Violation violation : violations) {
            violation.getLine();
            violation.getMessage();
            violation.getRuleName();
            violation.getReview();
        }

        return true;
    }
}
