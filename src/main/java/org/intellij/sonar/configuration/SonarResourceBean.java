package org.intellij.sonar.configuration;

import org.sonar.wsclient.services.Resource;

public class SonarResourceBean {
    public String name;
    public String key;
    public SonarQualifier qualifier;

    public SonarResourceBean() {
    }

    public SonarResourceBean(Resource resource) {
        setValuesFromResource(resource);
    }

    public void setValuesFromResource(Resource resource) {
        this.key = resource.getKey();
        this.name = resource.getName();
        this.qualifier = SonarQualifier.valueFrom(resource.getQualifier());
    }
}
