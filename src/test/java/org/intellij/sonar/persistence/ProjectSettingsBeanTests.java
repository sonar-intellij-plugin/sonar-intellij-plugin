package org.intellij.sonar.persistence;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.sonar.wsclient.services.Resource;

import static org.fest.assertions.Assertions.assertThat;

public class ProjectSettingsBeanTests {

  @Test
  public void shouldBeEqual() {
    final Resource resource1 = new Resource();
    resource1.setKey("key1");
    resource1.setName("name1");
    final Resource resource2 = new Resource();
    resource2.setKey("key2");
    resource2.setName("name2");

    ProjectSettingsBean bean1 = ProjectSettingsBean.of(
        "server",
        ImmutableList.of(resource1, resource2),
    ImmutableList.of(
        IncrementalScriptBean.of(ImmutableList.of("path1", "path2"),"my script","/path.to"),
        IncrementalScriptBean.of(ImmutableList.of("path3", "path4"),"my script 2","/path.to.2")
    ));

    ProjectSettingsBean bean2 = ProjectSettingsBean.of(
        "server",
        ImmutableList.of(resource1, resource2),
        ImmutableList.of(
            IncrementalScriptBean.of(ImmutableList.of("path1", "path2"),"my script","/path.to"),
            IncrementalScriptBean.of(ImmutableList.of("path3", "path4"),"my script 2","/path.to.2")
        ));

    assertThat(bean1).isEqualTo(bean2);

  }

}
