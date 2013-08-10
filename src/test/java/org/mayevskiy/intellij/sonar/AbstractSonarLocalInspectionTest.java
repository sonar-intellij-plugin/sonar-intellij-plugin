package org.mayevskiy.intellij.sonar;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Oleg Mayevskiy
 * @author Michail Plushnikov
 */
public abstract class AbstractSonarLocalInspectionTest extends LightCodeInsightFixtureTestCase {
  protected SonarLocalInspectionTool sonarLocalInspectionTool = new SonarLocalInspectionTool(null) {
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
//
//  @Override
//  protected String getTestDataPath() {
//    return "." + getBasePath();
//  }
}
