package org.intellij.sonar.persistence;

import java.util.Objects;

import static java.lang.String.format;

public class Resource {
    private String key;
    private String name;
    private String qualifier;

    public Resource() {
    }

    public Resource(String key, String name, String qualifier) {
        this.key = key;
        this.name = name;
        this.qualifier = qualifier;
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

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        Resource resource = (Resource) o;
        return Objects.equals(key, resource.key) &&
                Objects.equals(name, resource.name) &&
                Objects.equals(qualifier, resource.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, name, qualifier);
    }

    @Override
    public String toString() {
        return format("Resource{key='%s', name='%s', qualifier='%s'}", key, name, qualifier);
    }
}
