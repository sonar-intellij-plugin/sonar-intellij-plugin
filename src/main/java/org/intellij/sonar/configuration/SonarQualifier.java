package org.intellij.sonar.configuration;

import org.sonar.wsclient.services.Resource;

public enum SonarQualifier {
    PROJECT, MODULE;

    public static SonarQualifier valueFrom(String qualifier) {
        if (Resource.QUALIFIER_PROJECT.equals(qualifier)) {
            return PROJECT;
        } else if (Resource.QUALIFIER_MODULE.equals(qualifier)) {
            return MODULE;
        } else {
            throw new IllegalArgumentException(qualifier);
        }
    }
}
