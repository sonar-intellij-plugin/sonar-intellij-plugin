package org.intellij.sonar;

import com.intellij.codeInspection.LocalQuickFixBase;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sonar.wsclient.services.Violation;

import java.util.Collection;

/**
 * Author: Oleg Mayevskiy
 * Date: 20.11.13
 * Time: 00:01
 */
public class MarkAsFixedLocallyQuickFix extends LocalQuickFixBase {

  private String sonarFileResourceKey;
  private Violation violationToBeMarkedAsFixed;

  public MarkAsFixedLocallyQuickFix(@NotNull Violation violationToBeMarkedAsFixed, @NotNull String sonarFileResourceKey) {
    super("Mark as fixed locally - " + violationToBeMarkedAsFixed.getMessage(), "SonarQube");
    this.violationToBeMarkedAsFixed = violationToBeMarkedAsFixed;
    this.sonarFileResourceKey = sonarFileResourceKey;
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
    SonarViolationsProvider sonarViolationsProvider = ServiceManager.getService(project, SonarViolationsProvider.class);
    Collection<Violation> violationsOfFile = sonarViolationsProvider.mySonarViolations.get(sonarFileResourceKey);
    violationsOfFile.remove(violationToBeMarkedAsFixed);
  }
}
