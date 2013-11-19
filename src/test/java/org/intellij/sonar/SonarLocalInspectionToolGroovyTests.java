package org.intellij.sonar;

import com.intellij.psi.PsiFile;

import java.util.Collection;

public class SonarLocalInspectionToolGroovyTests extends AbstractSonarLocalInspectionTest {

  @Override
  protected String getBasePath() {
    return "/src/test/testdata/groovy";
  }

  public void testConvertPsiFileToSonarKeyForGroovyFileInRootDir() {
    PsiFile psiFile = myFixture.configureByFile("VeryBadClassRoot.groovy");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:[root]/VeryBadClassRoot.groovy"));
  }

  public void testConvertPsiFileToSonarKeyForGroovyFileInPackageDir() {
    PsiFile psiFile = myFixture.configureByFile("foo/bar/VeryBadClass.groovy");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:foo/bar/VeryBadClass.groovy"));
  }
}
