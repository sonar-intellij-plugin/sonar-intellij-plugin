package org.mayevskiy.intellij.sonar.service;

import org.mayevskiy.intellij.sonar.bean.SonarSettingsBean;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Violation;
import org.sonar.wsclient.services.ViolationQuery;

import java.util.List;

public class SonarService {
    public boolean testConnection(SonarSettingsBean sonarSettingsBean) {

        Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
        ViolationQuery violationQuery = ViolationQuery.createForResource(sonarSettingsBean.resource);
        violationQuery.setDepth(-1);
        violationQuery.setSeverities("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");
        List<Violation> violations = sonar.findAll(violationQuery);
        for (Violation violation : violations) {
            violation.getLine();
            violation.getMessage();
            violation.getRuleName();
            violation.getReview();
            violation.getResourceKey();
            violation.getResourceQualifier();
        }

        return true;
    }

    public List<Violation> getViolations(SonarSettingsBean sonarSettingsBean) {
        Sonar sonar = Sonar.create(sonarSettingsBean.host, sonarSettingsBean.user, sonarSettingsBean.password);
        ViolationQuery violationQuery = ViolationQuery.createForResource(sonarSettingsBean.resource);
        violationQuery.setDepth(-1);
        violationQuery.setSeverities("BLOCKER", "CRITICAL", "MAJOR", "MINOR", "INFO");

        return sonar.findAll(violationQuery);
    }
}
