package org.intellij.sonar;

import com.intellij.psi.PsiFile;

import java.util.Collection;

public class SonarLocalInspectionToolPhpTests extends AbstractSonarLocalInspectionTest {

  @Override
  protected String getBasePath() {
    return "/src/test/testdata/php";
  }

  public void testConvertPsiFileToSonarKeyForPhpFileInRootDir() {
    PsiFile psiFile = myFixture.configureByFile("Math.php");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:Math.php"));
  }

  public void testConvertPsiFileToSonarKeyForPhpFileInPackageDir() {
    PsiFile psiFile = myFixture.configureByFile("Bar/Math4.php");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:Bar/Math4.php"));
  }
}
