package org.intellij.sonar.util;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SonarComponentToFileMatcherTests {

  public static final String SONAR_PROJECT = "sonar:project";

  @Test
  public void php() {
    String component = "sonar:project:Bar/Math4.php";
    String fullPath = "/project/root/src/main/php/Bar/Math4.php";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }

  @Test
  public void javaDefaultPackage() {
    String component = "sonar:project:[default].OtherClass";
    String fullPath = "/project/root/src/main/java/OtherClass.java";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }

  @Test
  public void javaNonDefaultPackage() {
    String component = "sonar:project:foo.bar.Clazz";
    String fullPath = "/project/root/src/main/java/foo/bar/Clazz.java";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }

  @Test
  public void groovyDefaultPackage() {
    String component = "sonar:project:[root]/VeryBadClassRoot.groovy";
    String fullPath = "/project/root/src/main/groovy/VeryBadClassRoot.groovy";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }

  @Test
  public void groovyNonDefaultPackage() {
    String component = "sonar:project:foo/bar/VeryBadClass.groovy";
    String fullPath = "/project/root/src/main/groovy/foo/bar/VeryBadClass.groovy";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }

  @Test
  public void javaSquid() {
    String component = "sonar:project:src/main/java/org/sonar/batch/DefaultSensorContext.java";
    String fullPath = "/project/root/src/main/java/org/sonar/batch/DefaultSensorContext.java";
    assertThat(SonarComponentToFileMatcher.match(component, SONAR_PROJECT, fullPath)).isTrue();
  }
}
