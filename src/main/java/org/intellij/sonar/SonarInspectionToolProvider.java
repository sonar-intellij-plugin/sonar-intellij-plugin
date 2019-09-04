package org.intellij.sonar;

import com.intellij.codeInspection.InspectionToolProvider;
import org.intellij.sonar.analysis.NewIssuesGlobalInspectionTool;
import org.intellij.sonar.analysis.OldIssuesGlobalInspectionTool;
import org.jetbrains.annotations.NotNull;

public class SonarInspectionToolProvider implements InspectionToolProvider {

  @NotNull
  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{NewIssuesGlobalInspectionTool.class,OldIssuesGlobalInspectionTool.class};
  }
}
