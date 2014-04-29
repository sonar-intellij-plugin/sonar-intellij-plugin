package org.intellij.sonar.inspection;

import com.intellij.codeInspection.ExternalAnnotatorInspectionVisitor;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.psi.PsiFile;

public class SonarExternalAnnotatorInspectionVisitor extends ExternalAnnotatorInspectionVisitor {
  public SonarExternalAnnotatorInspectionVisitor(ProblemsHolder holder, ExternalAnnotator annotator, boolean onTheFly) {
    super(holder, annotator, onTheFly);

  }

  @Override
  public void visitFile(PsiFile file) {
    super.visitFile(file);
  }
}
