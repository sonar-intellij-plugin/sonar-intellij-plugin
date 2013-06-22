package org.mayevskiy.intellij.sonar;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Author: Oleg Mayevskiy
 * Date: 22.06.13
 * Time: 10:42
 */
public class SonarLocalInspectionToolPhpTests extends LightCodeInsightFixtureTestCase {

    private SonarLocalInspectionTool sonarLocalInspectionTool = new SonarLocalInspectionTool(null) {
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

    @Override
    protected String getTestDataPath() {
        return "../sonar-intellij-plugin/src/test/testdata/php";
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
