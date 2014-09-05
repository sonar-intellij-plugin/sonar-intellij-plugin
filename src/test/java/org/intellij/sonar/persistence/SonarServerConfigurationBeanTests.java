package org.intellij.sonar.persistence;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class SonarServerConfigurationBeanTests {

  @Test
  public void shouldBeEqualByName() {

    SonarServerConfig bean1 = SonarServerConfig.of(
        "my name",
        "http://site.my",
        true,
        "user"
    );

    SonarServerConfig bean2 = SonarServerConfig.of(
        "my name",
        "http://other.site",
        false,
        "other user"
    );

    assertThat(bean1).isEqualTo(bean2);
  }
}
