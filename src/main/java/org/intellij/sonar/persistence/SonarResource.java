package org.intellij.sonar.persistence;

import com.google.common.base.Objects;
import org.intellij.sonar.configuration.SonarQualifier;

public class SonarResource {

  private String key;
  private String name;

  public static SonarResource of(Resource resource) {
    SonarResource sonarResource = new SonarResource();
    if (SonarQualifier.MODULE.getQualifier().equals(resource.getQualifier())) {
      sonarResource.name = String.format("    %s",resource.getName());
    } else {
      sonarResource.name = resource.getName();
    }
    sonarResource.key = resource.getKey();
    return sonarResource;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarResource that = (SonarResource) o;
    return Objects.equal(key, that.key) &&
            Objects.equal(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, name);
  }

  @Override
  public String toString() {
    return "SonarResource{" +
            "key='" + key + '\'' +
            ", name='" + name + '\'' +
            '}';
  }
}
