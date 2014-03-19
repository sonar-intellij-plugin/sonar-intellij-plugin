package org.intellij.sonar.persistence;

import org.sonar.wsclient.services.Resource;

public class SonarResource {

  private String key;
  private String name;

  public SonarResource(Resource resource) {
    if (resource.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
      this.name = String.format("    %s", resource.getName());
    } else {
      this.name = resource.getName();
    }
    this.key = resource.getKey();
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "SonarResource{" +
        "name='" + name + '\'' +
        '}';
  }
}
