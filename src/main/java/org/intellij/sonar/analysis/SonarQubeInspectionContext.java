/*
 * SonarQube IntelliJ
 * Copyright (C) 2013-2014 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.intellij.sonar.analysis;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.intellij.sonar.DocumentChangeListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SonarQubeInspectionContext implements GlobalInspectionContextExtension<SonarQubeInspectionContext> {

  public static final Key<SonarQubeInspectionContext> KEY = Key.create("SonarQubeInspectionContext");

  @NotNull
  @Override
  public Key<SonarQubeInspectionContext> getID() {
    return KEY;
  }

  @Override
  public void performPreRunActivities(@NotNull List<Tools> globalTools, @NotNull List<Tools> localTools, @NotNull GlobalInspectionContext context) {
    // download issues
    // local analysis
    System.out.println("performPreRunActivities");
//    context.getRefManager().getScope() can be file, module or project
  }

  @Override
  public void performPostRunActivities(@NotNull List<InspectionToolWrapper> inspections, @NotNull GlobalInspectionContext context) {
    DocumentChangeListener.CHANGED_FILES.clear();

    removeAllHighlighters();

    // rerun external annotator and refresh highlighters in editor
    Project p = context.getProject();
    DaemonCodeAnalyzer.getInstance(p).restart();
  }

  private static void removeAllHighlighters() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        Editor[] allEditors = EditorFactory.getInstance().getAllEditors();
        for (Editor editor : allEditors) {
          editor.getMarkupModel().removeAllHighlighters();
        }
      }
    });
  }

  @Override
  public void cleanup() {}

}
