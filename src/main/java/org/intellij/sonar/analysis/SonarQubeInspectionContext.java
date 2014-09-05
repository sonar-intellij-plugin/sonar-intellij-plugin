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

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.DocumentChangeListener;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.util.SourceCodePlaceHolders;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SonarQubeInspectionContext implements GlobalInspectionContextExtension<SonarQubeInspectionContext> {

  public static final Key<SonarQubeInspectionContext> KEY = Key.create("SonarQubeInspectionContext");

  @NotNull
  @Override
  public Key<SonarQubeInspectionContext> getID() {
    return KEY;
  }

  @Override
  public void performPreRunActivities(@NotNull List<Tools> globalTools, @NotNull List<Tools> localTools, @NotNull final GlobalInspectionContext context) {
    // context.getRefManager().getScope() can be file, module or project
    // files -> modules settings of the files or project if no modules found
    // modules -> modules settings of the modules or project if no modules found
    // project -> project settings, but module settings for modules with different then project
    // ... start with project settings, the simplest use case

    // get settings based on context
    // download issues for all sonar resources from settings
    // run all local analysis scripts from settings (e.g. two modules with different scripts)
    //    AnalysisScope.MODULES
    //    context.getRefManager().getScope().getScopeType()
    //    context.getRefManager().getScope().toSearchScope() instanceof LocalSearchScope -> file
    //    context.getRefManager().getScope().toSearchScope() instanceof ModuleWithDependenciesScope -> module
    //    context.getRefManager().getScope().toSearchScope() instanceof ProjectScopeImpl -> project

    final Project project = context.getProject();
    final Collection<Module> modules = Sets.newHashSet();
    context.getRefManager().getScope().accept(new PsiElementVisitor() {
      @Override
      public void visitFile(PsiFile file) {
        final Module module = ModuleUtil.findModuleForPsiElement(file);
        modules.add(module);
      }
    });

    Set<Settings> settingsFromScope = Sets.newHashSet();
    if (modules.isEmpty()) {
      final Settings settings = ProjectSettings.getInstance(project).getState();
      settingsFromScope.add(settings);
    } else {
      for (Module module : modules) {
        final Settings settings = ModuleSettings.getInstance(module).getState();
        settingsFromScope.add(settings);
      }
    }

    for (Settings settings : settingsFromScope) {
      // download issues
    }

    for (Settings settings : settingsFromScope) {
      // run scripts
    }

    final int scopeType = context.getRefManager().getScope().getScopeType();
    if (AnalysisScope.PROJECT == scopeType) {

      // gather task settings
      // tasks can be:
      //                1) download issues from remote server
      //                   needs
      //                          SonarServers.get(settings.getServerName());
      //                          SonarServerConfiguration
      //                                hostName
      //                                user
      //                                password
      //                          settings.getResources()
      //                                resources.getKey()
      //                   -> loadIssues(...)
      //                2) run local analysis script
      //                   needs
      //                          LocalAnalysisScripts.get(settings.getLocalAnalysisScripName())
      //                          SourceCodePlaceHolders
      //                   -> runScript(sourceCodeWithReplacedPlaceHolders)

      final Settings settings = ProjectSettings.getInstance(project).getState();
      final Optional<SonarServerConfiguration> sonarServerConfiguration = SonarServers.get(settings.getServerName());

      final String localAnalysisScripName = settings.getLocalAnalysisScripName();
      final Optional<LocalAnalysisScript> script = LocalAnalysisScripts.get(localAnalysisScripName);
      final SourceCodePlaceHolders sourceCodePlaceHoldersBuilder = SourceCodePlaceHolders.builder();
      // replace place holders
      // start with PROJECT
      if (script.isPresent()) {
        final String rawSourceCode = script.get().getSourceCode();
        sourceCodePlaceHoldersBuilder
            .withSourceCode(rawSourceCode)
            .withProject(project);

        if (sonarServerConfiguration.isPresent()) {
          sourceCodePlaceHoldersBuilder.withSonarServerConfiguration(sonarServerConfiguration.get());
        }

        final String sourceCode = sourceCodePlaceHoldersBuilder.build();
        System.out.println(sourceCode);
        // execute script
      }
    }
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
