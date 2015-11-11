package org.intellij.sonar.persistence;

import org.sonar.wsclient.services.Resource;

public class SonarResource {

  private String key;
  private String name;

  public static SonarResource of(Resource resource) {
    SonarResource sonarResource = new SonarResource();
    if (resource.getQualifier().equals(Resource.QUALIFIER_MODULE)) {
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
  public String toString() {
    return "SonarResource{"+
      "name='"+name+'\''+
      '}';
  }

  @SuppressWarnings("RedundantIfStatement")
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SonarResource that = (SonarResource) o;
    if (key != null
      ? !key.equals(that.key)
      : that.key != null)
      return false;
    if (name != null
      ? !name.equals(that.name)
      : that.name != null)
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null
      ? key.hashCode()
      : 0;
    result = 31 * result+(name != null
      ? name.hashCode()
      : 0);
    return result;
  }
}
