package org.intellij.sonar.sonarserver.result;

import org.fest.assertions.Assertions;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ResourceWithProfileTests {

  @Test
  public void deserializationShouldWork() {
    final String json = "[\n" +
        "    {\n" +
        "      \"id\": 41222,\n" +
        "        \"key\": \"autoact:autoact-b2b-api_groovy\",\n" +
        "        \"name\": \"autoact-b2b-api_groovy\",\n" +
        "        \"scope\": \"PRJ\",\n" +
        "        \"qualifier\": \"BRC\",\n" +
        "        \"date\": \"2014-04-08T14:36:39+0200\",\n" +
        "        \"creationDate\": \"2013-11-07T11:27:49+0100\",\n" +
        "        \"lname\": \"autoact-b2b-api_groovy\",\n" +
        "        \"lang\": \"grvy\", \n" +
        "      \"version\": \"master\",\n" +
        "        \"description\": \"\",\n" +
        "        \"msr\": [\n" +
        "      {\n" +
        "        \"key\": \"profile\",\n" +
        "          \"val\": 10,\n" +
        "          \"frmt_val\": \"10.0\",\n" +
        "          \"data\": \"AutoAct\"  \n" +
        "      }\n" +
        "      ]\n" +
        "    }\n" +
        "    ]";


    final ResourceWithProfile[] resourceWithProfiles = ResourceWithProfile.gson.fromJson(json, ResourceWithProfile[].class);

    assertThat(resourceWithProfiles).isNotNull().hasSize(1);
    final ResourceWithProfile resourceWithProfile = resourceWithProfiles[0];

    assertThat(resourceWithProfile.getId()).isEqualTo(41222);
    assertThat(resourceWithProfile.getKey()).isEqualTo("autoact:autoact-b2b-api_groovy");
    assertThat(resourceWithProfile.getName()).isEqualTo("autoact-b2b-api_groovy");
    assertThat(resourceWithProfile.getScope()).isEqualTo("PRJ");
    assertThat(resourceWithProfile.getQualifier()).isEqualTo("BRC");
    assertThat(resourceWithProfile.getDate()).isEqualTo(new DateTime("2014-04-08T14:36:39+0200"));
    assertThat(resourceWithProfile.getCreationDate()).isEqualTo(new DateTime("2013-11-07T11:27:49+0100"));
    assertThat(resourceWithProfile.getLname()).isEqualTo("autoact-b2b-api_groovy");
    assertThat(resourceWithProfile.getLang()).isEqualTo("grvy");
    assertThat(resourceWithProfile.getVersion()).isEqualTo("master");
    assertThat(resourceWithProfile.getDescription()).isEqualTo("");
    assertThat(resourceWithProfile.getMsr()).isNotNull().hasSize(1);
    assertThat(resourceWithProfile.getMsr().get(0).getKey()).isEqualTo("profile");
    assertThat(resourceWithProfile.getMsr().get(0).getVal()).isEqualTo("10");
    assertThat(resourceWithProfile.getMsr().get(0).getFrmtVal()).isEqualTo("10.0");
    assertThat(resourceWithProfile.getMsr().get(0).getData()).isEqualTo("AutoAct");

  }
}
