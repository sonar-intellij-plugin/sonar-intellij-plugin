package org.intellij.sonar.configuration;

import org.intellij.sonar.persistence.Resource;

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
    String resourceQualifier = resource.getQualifier();
    this.qualifier = SonarQualifier.isValidQualifier(resourceQualifier) ? SonarQualifier.valueFrom(resourceQualifier) : null;
  }
}
