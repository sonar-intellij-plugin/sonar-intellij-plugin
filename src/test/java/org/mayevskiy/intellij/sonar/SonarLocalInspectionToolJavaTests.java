package org.mayevskiy.intellij.sonar;

import com.intellij.psi.PsiFile;

import java.util.Collection;

/**
 * @author Oleg Mayevskiy
 * @author Michail Plushnikov
 */
public class SonarLocalInspectionToolJavaTests extends AbstractSonarLocalInspectionTest {

  @Override
  protected String getBasePath() {
    return "/src/test/testdata/java";
  }

  public void testConvertPsiFileToSonarKeyForJavaFileInRootDir() {
    PsiFile psiFile = myFixture.configureByFile("OtherClass.java");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:[default].OtherClass"));
  }

  public void testConvertPsiFileToSonarKeyForJavaFileInPackageDir() {
    PsiFile psiFile = myFixture.configureByFile("foo/bar/Clazz.java");
    Collection<String> result = sonarLocalInspectionTool.getSonarKeyCandidatesForPsiFile(psiFile, "sonar:project");
    assertTrue(result.contains("sonar:project:foo.bar.Clazz"));
  }
}
