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
public class SonarLocalInspectionToolGroovyTests extends LightCodeInsightFixtureTestCase {

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
        return "../sonar-intellij-plugin/src/test/testdata/groovy";
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
