package org.intellij.sonar;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.intellij.sonar.inspection.SonarLocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSonarLocalInspectionTest extends LightCodeInsightFixtureTestCase {
  protected SonarLocalInspectionTool sonarLocalInspectionTool = new SonarLocalInspectionTool() {
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
      return "Sonar";
    }

    @Override
    public boolean isNew() {
      return false;
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
      return "sonar";
    }

    @NotNull
    @Override
    public String getStaticDescription() {
      return "desc";
    }

    @NotNull
    @Override
    public String getRuleKey() {
      return "ruleKey";
    }
  };

//  static {
//    // Change testdata directory to local root
//    System.setProperty(PathManager.PROPERTY_HOME_PATH, ".");
//  }

  @Override
  protected String getTestDataPath() {
    return "." + getBasePath();
  }
}
