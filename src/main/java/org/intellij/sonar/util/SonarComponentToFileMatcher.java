package org.intellij.sonar.util;

import com.intellij.openapi.util.Trinity;

import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;

public class SonarComponentToFileMatcher {

    /**
     * @param componentFromSonar              like sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java
     * @param resourceKeyFromConfiguration    like sonar:project
     * @param fullFilePathFromLocalFileSystem like /path/to/a.file
     * @return true if the sonar component corresponds to the file, false if not
     */
    public static boolean match(String componentFromSonar, String resourceKeyFromConfiguration, String fullFilePathFromLocalFileSystem) {
        final Trinity<String, String, String> args = new Trinity<String, String, String>(componentFromSonar, resourceKeyFromConfiguration, fullFilePathFromLocalFileSystem);
        final boolean matchResult = doMatch(componentFromSonar, resourceKeyFromConfiguration, fullFilePathFromLocalFileSystem);
        return matchResult;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean doMatch(String componentFromSonar, String resourceKeyFromConfiguration, String fullFilePathFromLocalFileSystem) {
        if (
                isEmptyOrSpaces(componentFromSonar)
                        || isEmptyOrSpaces(fullFilePathFromLocalFileSystem)
                ) {
            return false;
        }

        // component = "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java";
        // resourceKey = "sonar:project"
        if (!isEmptyOrSpaces(resourceKeyFromConfiguration) && !componentFromSonar.startsWith(resourceKeyFromConfiguration)) {
            return false;
        }

        // "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java"
        // -> "src/main/java/org/sonar/batch/DefaultSensorContext.java"
        final String fileKeyFromComponent;
        if (!isEmptyOrSpaces(resourceKeyFromConfiguration)) {
            fileKeyFromComponent = componentFromSonar.replace(resourceKeyFromConfiguration + ":", "");
        } else {
            fileKeyFromComponent = componentFromSonar.replaceAll("(?i)(.+:)(.+)", "$2");
        }

        if (fullFilePathFromLocalFileSystem.endsWith(fileKeyFromComponent)) {
            return true;
        }

        // [root]/VeryBadClassRoot.groovy
        // -> /VeryBadClassRoot.groovy
        final String normalizedFileKey;
        normalizedFileKey = fileKeyFromComponent.replaceAll("\\[.+\\]", "");

        if (fullFilePathFromLocalFileSystem.endsWith(normalizedFileKey)) {
            return true;
        }

        // .OtherClass
        // -> /OtherClass.java
        final String normalizedFileKeyForJava;
        normalizedFileKeyForJava = normalizedFileKey.replaceAll("\\.", "/") + ".java";

        if (fullFilePathFromLocalFileSystem.endsWith(normalizedFileKeyForJava)) {
            return true;
        }

        return false;
    }
}
