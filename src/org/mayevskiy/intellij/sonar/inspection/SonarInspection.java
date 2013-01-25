package org.mayevskiy.intellij.sonar.inspection;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:50
 */
public class SonarInspection extends BaseJavaLocalInspectionTool {
    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Sonar";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "SonarInspection";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "inspection from sonar";
    }

    @Nullable
    @Override
    public String getDescriptionFileName() {
        return "inspectionDescriptions/Sonar.html";
    }

    @Override
    public boolean runForWholeFile() {
        return super.runForWholeFile();
    }
}
