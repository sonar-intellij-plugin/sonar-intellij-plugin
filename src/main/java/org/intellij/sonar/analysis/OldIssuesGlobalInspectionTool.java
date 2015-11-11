package org.intellij.sonar.analysis;

import org.intellij.sonar.index.SonarIssue;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OldIssuesGlobalInspectionTool extends BaseGlobalInspectionTool {

  /**
   @see com.intellij.codeInspection.InspectionEP#groupDisplayName
   @see com.intellij.codeInspection.InspectionEP#groupKey
   @see com.intellij.codeInspection.InspectionEP#groupBundle
   */
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "SonarQube";
  }

  /**
   Override this method to return a html inspection description. Otherwise it will be loaded from resources using ID.

   @return hard-code inspection description.
   */
  @Nullable
  @Override
  public String getStaticDescription() {
    return "Reports issues found by SonarQube.";
  }

  @Override
  public Boolean processIssue(SonarIssue issue) {
    return !issue.getIsNew();
  }
}
