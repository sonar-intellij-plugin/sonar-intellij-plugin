package org.intellij.sonar.sonarreport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;

public class SonarReportTests {

  @Test
  public void convertJsonFileToJavaBeanShouldWork() throws IOException {
    URL url = Resources.getResource("sonar-report.json");
    String sonarReportContent = Resources.toString(url, Charsets.UTF_8);

    final SonarReport sonarReportFromFile = SonarReport.fromJson(sonarReportContent);

    final SonarReport expectedSonarReport = new SonarReport("4.1.1"
        , ImmutableList.of(
        new Issue(
            "002e8a9c-3d3d-495f-8a1a-06710ce70346",
            "de.mobile.dealer:dealer-admin:de.mobile.dealer.homepage.HomepageSettingsApplicationImpl",
            56,
            "The method 'updateSettings' has a Cyclomatic Complexity of 20.",
            "MAJOR",
            "pmd:CyclomaticComplexity",
            "OPEN",
            false,
            new DateTime("2013-11-20T18:28:49+0100"),
            new DateTime("2014-02-06T22:17:02+0100")
        )
        , new Issue(
            "15658627-d8eb-4310-abef-25bd5925815f",
            "de.mobile.dealer:dealer-admin:de.mobile.dealer.homepage.HomepageSettingsApplicationImpl",
            142,
            "Method 'EbayPictureService.findByBaseUri(...)' is deprecated.",
            "MAJOR",
            "squid:CallToDeprecatedMethod",
            "OPEN",
            false,
            new DateTime("2013-12-04T04:45:42+0100"),
            new DateTime("2014-02-06T22:17:02+0100")
        )
    ), ImmutableList.of(new Component("de.mobile.dealer:dealer-admin:de.mobile.dealer.homepage.HomepageSettingsApplicationImpl"))
        , ImmutableList.of(
        new Rule(
            "pmd:CyclomaticComplexity",
            "CyclomaticComplexity",
            "pmd",
            "Code size - cyclomatic complexity"
        )
        , new Rule(
            "squid:CallToDeprecatedMethod",
            "CallToDeprecatedMethod",
            "squid",
            "Avoid use of deprecated method"
        )
    )
        , Lists.<User>newArrayList()
    );

    assertThat(sonarReportFromFile).isEqualTo(expectedSonarReport);
  }
}
