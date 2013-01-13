package org.mayevskiy.intellij.sonar.service;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;

public class SonarService {
    public boolean testConnection(String host) {
        Sonar sonar = Sonar.create(host);
        Resource resource = sonar.find(ResourceQuery.createForMetrics("org.apache.struts:struts-parent", "coverage", "lines", "violations"));
        return true;
    }
}
