package org.intellij.sonar.sonarserver.result;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class ProfileTests {

 /* [
  {
    "name": "mobile_relaxed",
      "language": "java",  <--LANGUAGE
    "default": false,
      "rules": [
    {
      "key": "com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck",
        "repo": "checkstyle",  <-- RULE REPOSITORY
      "severity": "MAJOR"
    },
    {
      "key": "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
        "repo": "checkstyle",
        "severity": "MAJOR"
    },
    {
      "key": "com.puppycrawl.tools.checkstyle.checks.naming.StaticVariableNameCheck",
        "repo": "checkstyle",
        "severity": "MAJOR"
    },
    {
      "key": "com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck",
        "repo": "checkstyle",
        "severity": "INFO"
    },*/

  @Test
  public void deserializationShouldWork() {

    final String json = "[\n" +
        "    {\n" +
        "      \"name\": \"mobile_relaxed\",\n" +
        "        \"language\": \"java\",  \n" +
        "      \"default\": false,\n" +
        "        \"rules\": [\n" +
        "      {\n" +
        "        \"key\": \"com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck\",\n" +
        "          \"repo\": \"checkstyle\",  \n" +
        "        \"severity\": \"MAJOR\"\n" +
        "      },\n" +
        "      {\n" +
        "        \"key\": \"com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck\",\n" +
        "          \"repo\": \"checkstyle\",\n" +
        "          \"severity\": \"MAJOR\"\n" +
        "      }" +
        "      ]" +
        "     }" +
        "]";
    final Profile[] profiles = Profile.gson.fromJson(json, Profile[].class);

    assertThat(profiles).isNotNull().hasSize(1);

    Profile profile = profiles[0];
    assertThat(profile).isNotNull();
    assertThat(profile.getName()).isEqualTo("mobile_relaxed");
    assertThat(profile.getLanguage()).isEqualTo("java");
    assertThat(profile.getIsDefaultProfile()).isFalse();
    assertThat(profile.getRules()).isNotNull().hasSize(2);

    List<Rule> rules = profile.getRules();
    assertThat(rules.get(0).getKey()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck");
    assertThat(rules.get(0).getRepo()).isEqualTo("checkstyle");
    assertThat(rules.get(0).getSeverity()).isEqualTo("MAJOR");
    assertThat(rules.get(1).getKey()).isEqualTo("com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck");
    assertThat(rules.get(1).getRepo()).isEqualTo("checkstyle");
    assertThat(rules.get(1).getSeverity()).isEqualTo("MAJOR");
  }
}
