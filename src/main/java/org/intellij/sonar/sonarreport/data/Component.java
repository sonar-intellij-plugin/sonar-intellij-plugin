package org.intellij.sonar.sonarreport.data;

public class Component {

  private final String key;

  public Component(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Component component = (Component) o;

    if (key != null ? !key.equals(component.key) : component.key != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Component{" +
        "key='" + key + '\'' +
        '}';
  }
}
