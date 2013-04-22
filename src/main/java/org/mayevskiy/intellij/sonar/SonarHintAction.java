package org.mayevskiy.intellij.sonar;

import com.intellij.codeInspection.HintAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Author: Oleg Mayevskiy
 * Date: 11.04.13
 * Time: 15:28
 */
public class SonarHintAction implements HintAction {
    @Override
    public boolean showHint(Editor editor) {
        return true;
    }

    @NotNull
    @Override
    public String getText() {
        return "my text";
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "my family name";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
