package org.intellij.sonar.util;

import com.google.common.base.Optional;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.text.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SonarComponentToFileMatcher {

  private final static Map<String, String> FILE_KEY_FROM_COMPONENT_CACHE = new ConcurrentHashMap<String, String>();
  private final static Map<String, String> NORMALIZED_FILE_KEY_CACHE = new ConcurrentHashMap<String, String>();
  private final static Map<String, String> NORMALIZED_FILE_KEY_FOR_JAVA_CACHE = new ConcurrentHashMap<String, String>();
  private final static Map<Trinity<String, String, String>, Boolean> MATCH_CACHE = new ConcurrentHashMap<Trinity<String, String, String>, Boolean>();

  /**
   *
   * @param componentFromSonar like sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java
   * @param resourceKeyFromConfiguration like sonar:project
   * @param fullFilePathFromLocalFileSystem like /path/to/a.file
   * @return true if the sonar component corresponds to the file, false if not
   */
  public static boolean match(String componentFromSonar, String resourceKeyFromConfiguration, String fullFilePathFromLocalFileSystem) {
    final Trinity<String, String, String> args = new Trinity<String, String, String>(componentFromSonar, resourceKeyFromConfiguration, fullFilePathFromLocalFileSystem);
    final Optional<Boolean> matchResultFromCache = Optional.fromNullable(MATCH_CACHE.get(args));
    if (matchResultFromCache.isPresent()) {
      return matchResultFromCache.get();
    } else {
      final boolean matchResult = doMatch(componentFromSonar, resourceKeyFromConfiguration, fullFilePathFromLocalFileSystem);
      MATCH_CACHE.put(args, matchResult);
      return matchResult;
    }
  }

  private static boolean doMatch(String componentFromSonar, String resourceKeyFromConfiguration, String fullFilePathFromLocalFileSystem) {
    if (
        StringUtil.isEmptyOrSpaces(componentFromSonar)
            || StringUtil.isEmptyOrSpaces(resourceKeyFromConfiguration)
            || StringUtil.isEmptyOrSpaces(fullFilePathFromLocalFileSystem)
        ) {
      return false;
    }

    // component = "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java";
    // resourceKey = "sonar:project"
    if (!componentFromSonar.startsWith(resourceKeyFromConfiguration)) {
      return false;
    }

    // "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java"
    // -> "src/main/java/org/sonar/batch/DefaultSensorContext.java"
    final Optional<String> fileKeyFromComponentFromCache = Optional.fromNullable(FILE_KEY_FROM_COMPONENT_CACHE.get(componentFromSonar));

    final String fileKeyFromComponent;
    if (fileKeyFromComponentFromCache.isPresent()) {
      fileKeyFromComponent = fileKeyFromComponentFromCache.get();
    } else {
      fileKeyFromComponent = componentFromSonar.replace(resourceKeyFromConfiguration + ":", "");
      FILE_KEY_FROM_COMPONENT_CACHE.put(componentFromSonar, fileKeyFromComponent);
    }

    if (fullFilePathFromLocalFileSystem.endsWith(fileKeyFromComponent)) {
      return true;
    }

    // [root]/VeryBadClassRoot.groovy
    // -> /VeryBadClassRoot.groovy
    final Optional<String> normalizedFileKeyFromCache = Optional.fromNullable(NORMALIZED_FILE_KEY_CACHE.get(fileKeyFromComponent));
    final String normalizedFileKey;
    if (normalizedFileKeyFromCache.isPresent()) {
      normalizedFileKey = normalizedFileKeyFromCache.get();
    } else {
      normalizedFileKey = fileKeyFromComponent.replaceAll("\\[.+\\]", "");
      NORMALIZED_FILE_KEY_CACHE.put(fileKeyFromComponent, normalizedFileKey);
    }
    if (fullFilePathFromLocalFileSystem.endsWith(normalizedFileKey)) {
      return true;
    }

    // .OtherClass
    // -> /OtherClass.java
    final Optional<String> normalizedFileKeyForJavaFromCache = Optional.fromNullable(NORMALIZED_FILE_KEY_FOR_JAVA_CACHE.get(normalizedFileKey));
    final String normalizedFileKeyForJava;
    if (normalizedFileKeyForJavaFromCache.isPresent()) {
      normalizedFileKeyForJava = normalizedFileKeyForJavaFromCache.get();
    } else {
      normalizedFileKeyForJava = normalizedFileKey.replaceAll("\\.", "/") + ".java";
      NORMALIZED_FILE_KEY_FOR_JAVA_CACHE.put(normalizedFileKey, normalizedFileKeyForJava);
    }
    if (fullFilePathFromLocalFileSystem.endsWith(normalizedFileKeyForJava)) {
      return true;
    }

    return false;
  }
}
