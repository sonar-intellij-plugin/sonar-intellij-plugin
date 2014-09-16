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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.DocumentChangeListener;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.index.IssuesByFileIndex;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;
import org.intellij.sonar.persistence.ModuleSettings;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Settings;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class SonarQubeInspectionContext implements GlobalInspectionContextExtension<SonarQubeInspectionContext> {

  public static final Key<SonarQubeInspectionContext> KEY = Key.create("SonarQubeInspectionContext");

  @NotNull
  @Override
  public Key<SonarQubeInspectionContext> getID() {
    return KEY;
  }

  public static class EnrichedSettings {
    public Settings settings;
    public Project project;
    public Module module;

    public EnrichedSettings(Settings settings, Project project, Module module) {
      this.settings = settings;
      this.project = project;
      this.module = module;

    }
  }

  @Override
  public void performPreRunActivities(@NotNull List<Tools> globalTools, @NotNull List<Tools> localTools, @NotNull final GlobalInspectionContext context) {

    final Project project = context.getProject();
    SonarConsole.get(project).clear();
    final Set<Module> modules = Sets.newHashSet();
    final ImmutableList.Builder<PsiFile> filesBuilder = ImmutableList.builder();

    context.getRefManager().getScope().accept(new PsiElementVisitor() {
      @Override
      public void visitFile(PsiFile psiFile) {
        filesBuilder.add(psiFile);
        final Module module = ModuleUtil.findModuleForPsiElement(psiFile);
        if (module != null) modules.add(module);
      }
    });
    final ImmutableList<PsiFile> psiFiles = filesBuilder.build();
    IssuesByFileIndex.clearIndexFor(psiFiles);

    Set<EnrichedSettings> enrichedSettingsFromScope = Sets.newHashSet();
    if (modules.isEmpty() || AnalysisScope.PROJECT == context.getRefManager().getScope().getScopeType()) {
      final Settings settings = ProjectSettings.getInstance(project).getState();
      enrichedSettingsFromScope.add(new EnrichedSettings(settings, project, null));
    } else {
      for (Module module : modules) {
        final Settings settings = ModuleSettings.getInstance(module).getState();
        enrichedSettingsFromScope.add(new EnrichedSettings(settings, project, module));
      }
    }

    for (final EnrichedSettings enrichedSettings : enrichedSettingsFromScope) {
      final Optional<DownloadIssuesTask> downloadTask = DownloadIssuesTask.from(enrichedSettings, psiFiles);
      if (downloadTask.isPresent()) {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          @Override
          public void run() {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                downloadTask.get(), "Downloading Issues", true, project
            );
          }
        }, ModalityState.NON_MODAL);
      }
    }

    for (final EnrichedSettings enrichedSettings : enrichedSettingsFromScope) {
      final Optional<RunLocalAnalysisScriptTask> scriptTask = RunLocalAnalysisScriptTask.from(enrichedSettings, psiFiles);
      if (scriptTask.isPresent()) {
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
          @Override
          public void run() {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                scriptTask.get(), "Running Local Analysis", true, project
            );
          }
        }, ModalityState.NON_MODAL);
      }
    }

  }

  @Override
  public void performPostRunActivities(@NotNull List<InspectionToolWrapper> inspections, @NotNull GlobalInspectionContext context) {
    DocumentChangeListener.CHANGED_FILES.clear();

    final Project project = context.getProject();
    final Optional<IssuesByFileIndexProjectComponent> indexComponent = IssuesByFileIndexProjectComponent.getInstance(project);
    if (indexComponent.isPresent()) {
      NotificationManager.getInstance(project)
          .showNotificationFor(indexComponent.get().getIndex());
    }

    // rerun external annotator and refresh highlighters in editor
    removeAllHighlighters();
    DaemonCodeAnalyzer.getInstance(project).restart();
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
