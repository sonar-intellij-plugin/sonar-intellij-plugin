package org.intellij.sonar;

import com.intellij.codeInspection.InspectionToolProvider;
import org.intellij.sonar.analysis.NewIssuesGlobalInspectionTool;
import org.intellij.sonar.analysis.OldIssuesGlobalInspectionTool;

public class SonarInspectionToolProvider implements InspectionToolProvider {

  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{NewIssuesGlobalInspectionTool.class, OldIssuesGlobalInspectionTool.class};
  }

}
