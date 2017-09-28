package org.intellij.sonar.configuration;

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;

public enum SonarQualifier {
    PROJECT("TRK"), MODULE("BRC");

    private static final Map<String, SonarQualifier> INSTANCE_BY_QUALIFIER = stream(values()).collect(toMap(SonarQualifier::getQualifier, identity()));

    private String qualifier;

    SonarQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getQualifier() {
        return qualifier;
    }

    public static SonarQualifier valueFrom(String qualifier) {
        return Optional.ofNullable(INSTANCE_BY_QUALIFIER.get(qualifier)).orElseThrow(() -> new IllegalArgumentException(qualifier));
    }

    public static boolean isValidQualifier(String qualifier) {
        return INSTANCE_BY_QUALIFIER.containsKey(qualifier);
    }
}
