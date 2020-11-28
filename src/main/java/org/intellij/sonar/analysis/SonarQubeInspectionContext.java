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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.DocumentChangeListener;
import org.intellij.sonar.console.SonarConsole;
import org.intellij.sonar.console.SonarToolWindowFactory;
import org.intellij.sonar.index.IssuesByFileIndex;
import org.intellij.sonar.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class SonarQubeInspectionContext implements GlobalInspectionContextExtension<SonarQubeInspectionContext> {

  public static final String SONAR_QUBE = "SonarQube";
  public static final String GROUP_ID = SONAR_QUBE;
  public static final String TITLE = SONAR_QUBE;

  private static final Key<SonarQubeInspectionContext> KEY = Key.create("SonarQubeInspectionContext");

  @NotNull
  @Override
  public Key<SonarQubeInspectionContext> getID() {
    return KEY;
  }

  public static class EnrichedSettings {

    public Settings settings;
    public Project project;
    public Module module;

    public EnrichedSettings(Settings settings,Project project,Module module) {
      this.settings = settings;
      this.project = project;
      this.module = module;
    }
  }

  @Override
  public void performPreRunActivities(
    @NotNull List<Tools> globalTools,
    @NotNull List<Tools> localTools,
    @NotNull final GlobalInspectionContext context
  ) {
    new InspectionToolsProcessor(context)
            .runInspectionTools();
  }

  private class InspectionToolsProcessor {
    private final GlobalInspectionContext context;
    private boolean newIssuesGlobalInspectionToolEnabled;
    private boolean oldIssuesGlobalInspectionToolEnabled;
    private Project project;
    private Set<Module> modules;
    private ImmutableList<PsiFile> psiFiles;
    private Set<EnrichedSettings> enrichedSettingsFromScope;

    InspectionToolsProcessor(GlobalInspectionContext context) {
      this.context = context;
    }

    void runInspectionTools() {
      initProject();
      collectModulesAndFiles();
      buildEnrichedSettingsFromScope();
      checkIsNewIssuesInspectionToolEnabled();
      checkIsOldIssuesInspectionToolEnabled();

      if (!anyInspectionToolEnabled()) {
        SonarConsole.get(project).info("No Sonar Inspections enabled.");
        return;
      }

      saveAllDocuments();
      showSonarQubeToolWindowIfNeeded();
      SonarConsole.get(project).clear();
      IssuesByFileIndex.clearIndexFor(psiFiles);
      downloadOldIssues();
      runLocalAnalysisScriptForNewIssues();
    }

    private void checkIsNewIssuesInspectionToolEnabled() {
      Predicate<EnrichedSettings> filter = s -> !LocalAnalysisScripts.NO_LOCAL_ANALYSIS.equals(s.settings.getLocalAnalysisScripName());
      newIssuesGlobalInspectionToolEnabled = settingsFromScopeAnyMatch(filter);
    }

    private void checkIsOldIssuesInspectionToolEnabled() {
      Predicate<EnrichedSettings> filter = s -> !SonarServers.NO_SONAR.equals(s.settings.getServerName());
      oldIssuesGlobalInspectionToolEnabled = settingsFromScopeAnyMatch(filter);
    }

    private boolean settingsFromScopeAnyMatch(Predicate<EnrichedSettings> filter) {
      return enrichedSettingsFromScope.stream().anyMatch(filter);
    }

    private boolean anyInspectionToolEnabled() {
      return newIssuesGlobalInspectionToolEnabled || oldIssuesGlobalInspectionToolEnabled;
    }

    private void saveAllDocuments() {
      ApplicationManager.getApplication().invokeAndWait(
              () -> FileDocumentManager.getInstance().saveAllDocuments()
      );
    }

    private void initProject() {
      project = context.getProject();
    }

    private void showSonarQubeToolWindowIfNeeded() {
      if (SonarConsoleSettings.getInstance().isShowSonarConsoleOnAnalysis()) {
        ApplicationManager.getApplication().invokeLater(
                () -> {
                  final ToolWindow toolWindow = ToolWindowManager.getInstance(project)
                          .getToolWindow(SonarToolWindowFactory.TOOL_WINDOW_ID);
                  Optional.ofNullable(toolWindow).ifPresent(window -> window.show(null));
                }
        );
      }
    }

    private void collectModulesAndFiles() {
      modules = Sets.newHashSet();
      final ImmutableList.Builder<PsiFile> filesBuilder = ImmutableList.builder();
      Objects.requireNonNull(context.getRefManager().getScope()).accept(
              new PsiElementVisitor() {
                @Override
                public void visitFile(@NotNull PsiFile psiFile) {
                  filesBuilder.add(psiFile);
                  final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
                  if (module != null) modules.add(module);
                }
              }
      );

      psiFiles = filesBuilder.build();
    }

    private void buildEnrichedSettingsFromScope() {
      enrichedSettingsFromScope = new HashSet<>();
      if (isProjectScope()) {
        addProjectSettings();
      } else {
        addModulesSettings();
      }
    }

    private boolean isProjectScope() {
      return modules.isEmpty() || selectedScopeIsProject() || inCustomScopeProjectIsSelected();
    }

    private boolean selectedScopeIsProject() {
      return isSelectedScope(AnalysisScope.PROJECT);
    }

    private boolean inCustomScopeProjectIsSelected(){
      return isSelectedScope(AnalysisScope.CUSTOM) && isProjectSelected();
    }

    private boolean isSelectedScope(int scopeType) {
      return scopeType == Objects.requireNonNull(context.getRefManager().getScope()).getScopeType();
    }

    private boolean isProjectSelected(){
      return "Project Files".equals(Objects.requireNonNull(context.getRefManager().getScope()).getDisplayName());
    }

    private void addProjectSettings() {
      final Settings settings = getProjectSettings();
      EnrichedSettings enrichedSettings = createEnrichedSettings(null, settings);
      enrichedSettingsFromScope.add(enrichedSettings);
    }

    @Nullable
    private Settings getProjectSettings() {
      return ProjectSettings.getInstance(project).getState();
    }

    private void addModulesSettings() {
      final Settings projectSettings = getProjectSettings();

      for (Module module : modules) {
        final Settings settings = getModuleSettings(module);
        insertProjectSettingsIfConfigured(projectSettings, settings);
        EnrichedSettings enrichedSettings = createEnrichedSettings(module, settings);
        enrichedSettingsFromScope.add(enrichedSettings);
      }
    }

    @NotNull
    private SonarQubeInspectionContext.EnrichedSettings createEnrichedSettings(Module module, Settings settings) {
      return new EnrichedSettings(settings, project, module);
    }

    @Nullable
    private Settings getModuleSettings(Module module) {
      return ModuleSettings.getInstance(module).getState();
    }

    private void insertProjectSettingsIfConfigured(Settings projectSettings, Settings moduleSettings){
      updateServerNameSettings(projectSettings, moduleSettings);
      updateLocalAnalysisScriptsSettings(projectSettings, moduleSettings);
    }

    private void updateServerNameSettings(Settings projectSettings, Settings moduleSettings) {
      String serverName = moduleSettings.getServerName();

      if(SonarServers.PROJECT.equals(serverName)){
        moduleSettings.setServerName(projectSettings.getServerName());
      }
    }

    private void updateLocalAnalysisScriptsSettings(Settings projectSettings, Settings moduleSettings) {
      String scriptName = moduleSettings.getLocalAnalysisScripName();

      if(LocalAnalysisScripts.PROJECT.equals(scriptName)){
        moduleSettings.setLocalAnalysisScripName(projectSettings.getLocalAnalysisScripName());
      }
    }

    private void downloadOldIssues() {
      if (oldIssuesGlobalInspectionToolEnabled) {
        for (final EnrichedSettings settings : enrichedSettingsFromScope) {
          runDownloadTaskFrom(settings);
        }
      }
    }

    private void runDownloadTaskFrom(EnrichedSettings enrichedSettings) {
      final Optional<DownloadIssuesTask> downloadTask = DownloadIssuesTask.from(project, enrichedSettings, psiFiles);
      if (downloadTask.isPresent()) {
        downloadTask.get().run();
      } else {
        Notifications.Bus.notify(new Notification(
            GROUP_ID, TITLE,
                "SonarQube is enabled, but the sonar server is not configured. Aborting...",
                NotificationType.ERROR
        ));
      }
    }

    private void runLocalAnalysisScriptForNewIssues() {
      if (newIssuesGlobalInspectionToolEnabled) {
        for (final EnrichedSettings settings : enrichedSettingsFromScope) {
          runScriptTaskFrom(settings);
        }
      }
    }

    private void runScriptTaskFrom(EnrichedSettings enrichedSettings) {
      final Optional<RunLocalAnalysisScriptTask> scriptTask = RunLocalAnalysisScriptTask.from(
              enrichedSettings,
              psiFiles
      );
      if (scriptTask.isPresent()) {
        scriptTask.get().run();
      } else {
        Notifications.Bus.notify(new Notification(
            GROUP_ID,TITLE,
                "SonarQube (new issues) is enabled, but the local analysis script is not configured. Aborting...",
                NotificationType.ERROR
        ));
      }
    }

  }

  @Override
  public void performPostRunActivities(
    @NotNull List<InspectionToolWrapper<?, ?>> inspections,
    @NotNull GlobalInspectionContext context
  ) {
    DocumentChangeListener.CHANGED_FILES.clear();
    final Project project = context.getProject();
    // rerun external annotator and refresh highlighters in editor
    removeAllHighlighters();
    DaemonCodeAnalyzer.getInstance(project).restart();
  }

  private static void removeAllHighlighters() {
    ApplicationManager.getApplication().invokeLater(
        () -> {
          Editor[] allEditors = EditorFactory.getInstance().getAllEditors();
          for (Editor editor : allEditors) {
            editor.getMarkupModel().removeAllHighlighters();
          }
        }
    );
  }

  @Override
  public void cleanup() {
    // do nothing
  }
}
