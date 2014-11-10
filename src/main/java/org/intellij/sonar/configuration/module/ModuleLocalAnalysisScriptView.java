package org.intellij.sonar.configuration.module;

import com.intellij.openapi.project.Project;
import org.intellij.sonar.configuration.partials.LocalAnalysisScriptView;
import org.intellij.sonar.persistence.LocalAnalysisScript;
import org.intellij.sonar.persistence.LocalAnalysisScripts;

import javax.swing.*;
import java.util.Collection;

import static org.intellij.sonar.util.UIUtil.makeObj;

public class ModuleLocalAnalysisScriptView extends LocalAnalysisScriptView {

    public ModuleLocalAnalysisScriptView(JComboBox localAnalysisScriptComboBox, JButton addLocalAnalysisScriptButton, JButton editLocalAnalysisScriptButton, JButton removeLocalAnalysisScriptButton, Project project) {
        super(localAnalysisScriptComboBox, addLocalAnalysisScriptButton, editLocalAnalysisScriptButton, removeLocalAnalysisScriptButton, project);
    }

    protected void initComboBox() {
        final Collection<LocalAnalysisScript> allScripts = LocalAnalysisScripts.getAll();
        myLocalAnalysisScriptComboBox.removeAllItems();
        myLocalAnalysisScriptComboBox.addItem(makeObj(LocalAnalysisScripts.PROJECT));
        myLocalAnalysisScriptComboBox.addItem(makeObj(LocalAnalysisScripts.NO_LOCAL_ANALYSIS));
        for (LocalAnalysisScript script : allScripts) {
            myLocalAnalysisScriptComboBox.addItem(makeObj(script.getName()));
        }
    }

    protected boolean editAndRemoveButtonsCanBeEnabled() {
        final boolean isNoLocalAnalysis = LocalAnalysisScripts.NO_LOCAL_ANALYSIS.equals(myLocalAnalysisScriptComboBox.getSelectedItem().toString());
        final boolean isProject = LocalAnalysisScripts.PROJECT.equals(myLocalAnalysisScriptComboBox.getSelectedItem().toString());
        return !isNoLocalAnalysis && !isProject;
    }
}
