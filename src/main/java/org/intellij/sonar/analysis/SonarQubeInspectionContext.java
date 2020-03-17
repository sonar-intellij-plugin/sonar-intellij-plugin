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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInspection.GlobalInspectionContext;
import com.intellij.codeInspection.ex.GlobalInspectionContextBase;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolWrapper;
import com.intellij.codeInspection.ex.Tools;
import com.intellij.codeInspection.lang.GlobalInspectionContextExtension;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
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
import org.intellij.sonar.persistence.ModuleSettings;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.persistence.SonarConsoleSettings;
import org.jetbrains.annotations.NotNull;

public class SonarQubeInspectionContext implements GlobalInspectionContextExtension<SonarQubeInspectionContext> {

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

  private boolean isInspectionToolEnabled(final String toolName,final GlobalInspectionContextBase context) {
    final InspectionProfileImpl currentProfile = context.getCurrentProfile();
    final Project project = context.getProject();
    return currentProfile.getAllEnabledInspectionTools(project).stream()
        .map(Tools::getShortName)
        .anyMatch(toolName::equals);
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
      checkIsNewIssuesGlobalInspectionToolEnabled();
      checkIsOldIssuesGlobalInspectionToolEnabled();
      if (!anyInspectionToolEnabled())
        return;
      saveAllDocuments();
      initProject();
      showSonarQubeToolWindowIfNeeded();
      SonarConsole.get(project).clear();
      collectModulesAndFiles();
      IssuesByFileIndex.clearIndexFor(psiFiles);
      buildEnrichedSettingsFromScope();
      downloadOldIssues();
      runLocalAnalysisScriptForNewIssues();
    }

    private void checkIsNewIssuesGlobalInspectionToolEnabled() {
      newIssuesGlobalInspectionToolEnabled = isInspectionToolEnabled(
              NewIssuesGlobalInspectionTool.class.getSimpleName(),
              (GlobalInspectionContextBase) context
      );
    }

    private void checkIsOldIssuesGlobalInspectionToolEnabled() {
      oldIssuesGlobalInspectionToolEnabled = isInspectionToolEnabled(
              OldIssuesGlobalInspectionTool.class.getSimpleName(),
              (GlobalInspectionContextBase)context
      );
    }

    private boolean anyInspectionToolEnabled() {
      return newIssuesGlobalInspectionToolEnabled || oldIssuesGlobalInspectionToolEnabled;
    }

    private void saveAllDocuments() {
      TransactionGuard.getInstance().submitTransactionAndWait(
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
                  toolWindow.show(null);
                }
        );
      }
    }

    private void collectModulesAndFiles() {
      modules = Sets.newHashSet();
      final ImmutableList.Builder<PsiFile> filesBuilder = ImmutableList.builder();
      context.getRefManager().getScope().accept(
              new PsiElementVisitor() {
                @Override
                public void visitFile(PsiFile psiFile) {
                  filesBuilder.add(psiFile);
                  final Module module = ModuleUtil.findModuleForPsiElement(psiFile);
                  if (module != null) modules.add(module);
                }
              }
      );

      psiFiles = filesBuilder.build();
    }

    private void buildEnrichedSettingsFromScope() {
      enrichedSettingsFromScope = Sets.newHashSet();
      if (isProjectScope()) {
        addProjectSettings();
      } else {
        addModulesSettings();
      }
    }

    private boolean isProjectScope() {
      return modules.isEmpty() || AnalysisScope.PROJECT == context.getRefManager().getScope().getScopeType();
    }

    private void addProjectSettings() {
      final Settings settings = ProjectSettings.getInstance(project).getState();
      enrichedSettingsFromScope.add(new EnrichedSettings(settings, project,null));
    }

    private void addModulesSettings() {
      for (Module module : modules) {
        final Settings settings = ModuleSettings.getInstance(module).getState();
        enrichedSettingsFromScope.add(new EnrichedSettings(settings, project,module));
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
      final Optional<DownloadIssuesTask> downloadTask = DownloadIssuesTask.from(enrichedSettings, psiFiles);
      if (downloadTask.isPresent()) {
        downloadTask.get().run();
      } else {
        Notifications.Bus.notify(new Notification(
                "SonarQube","SonarQube",
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
                "SonarQube","SonarQube",
                "SonarQube (new issues) is enabled, but the local analysis script is not configured. Aborting...",
                NotificationType.ERROR
        ));
      }
    }

  }

  @Override
  public void performPostRunActivities(
    @NotNull List<InspectionToolWrapper> inspections,
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
  }
}
