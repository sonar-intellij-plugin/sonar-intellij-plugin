package org.intellij.sonar.sonarreport.data;

import com.google.common.base.Objects;

public class Component {

  private final String key;
  private final String path;

  public Component(String key,String path) {
    this.key = key;
    this.path = path;
  }

  public String getKey() {
    return key;
  }

  public String getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Component component = (Component) o;
    return Objects.equal(key, component.key) &&
            Objects.equal(path, component.path);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(key, path);
  }

  @Override
  public String toString() {
    return "Component{" +
            "key='" + key + '\'' +
            ", path='" + path + '\'' +
            '}';
  }

}
