package org.intellij.sonar.analysis;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.intellij.codeInsight.highlighting.TooltipLinkHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.index.SonarIssue;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectService;
import org.intellij.sonar.persistence.ModuleSettings;
import org.intellij.sonar.persistence.ProjectSettings;
import org.intellij.sonar.persistence.Settings;
import org.intellij.sonar.persistence.SonarRules;
import org.intellij.sonar.persistence.SonarServerConfig;
import org.intellij.sonar.persistence.SonarServers;
import org.intellij.sonar.sonarserver.Rule;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IssueDescriptionLinkHandler extends TooltipLinkHandler {

  @Nullable
  @Override
  public String getDescription(
    @NotNull final String sonarIssueKey,
    @NotNull final Editor editor
  ) {
      return new SonarIssueDescription()
              .buildFrom(sonarIssueKey, editor)
              .getDescription();
  }

  private static class SonarIssueDescription {

    private String sonarIssueKey;
    private Editor editor;
    private Project project;
    private PsiFile psiFile;
    private IssuesByFileIndexProjectService state;
    private SonarIssue sonarIssue;
    private SonarRules sonarRules;
    private Settings settings;
    private boolean processing;
    private String description;
    private SonarServerConfig serverConfig;
    private Rule persistedRule;

    SonarIssueDescription() {
      this.processing = false;
    }

    SonarIssueDescription buildFrom(String sonarIssueKey, Editor editor) {
      this.sonarIssueKey = sonarIssueKey;
      this.editor = editor;
      this.description = null;
      this.processing = true;
      getProject();
      if (processing) getPsiFile();
      if (processing) getIssuesByFileIndexProjectComponent();
      if (processing) getSonarIssue();
      if (processing) getDescriptionFromFetchedRules();
      if (processing) getSettings();
      if (processing) getServerConfig();
      if (processing) persistRule();
      if (processing) getDescriptionFromPersistedRule();
      return this;
    }

    private void getProject() {
      project = editor.getProject();
      if (project == null || project.isDisposed()) {
        processing = false;
      }
    }

    private void getPsiFile() {
      psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
      if (psiFile == null) {
        processing = false;
      }
    }

    private void getIssuesByFileIndexProjectComponent() {
      Optional<IssuesByFileIndexProjectService> issuesByFileIndexProjectComponent = IssuesByFileIndexProjectService.getInstance(project);
      if (!issuesByFileIndexProjectComponent.isPresent()) {
        processing = false;
        return;
      }
      state = issuesByFileIndexProjectComponent.get().getState();
      if (state == null) {
        processing = false;
      }
    }

    private void getSonarIssue() {
      final Map<String, Set<SonarIssue>> index = state.getIndex();
      final String path = psiFile.getVirtualFile().getPath();
      final Set<SonarIssue> issues = index.get(path);
      sonarIssue = issues.stream()
              .filter(issue -> sonarIssueKey.equals(issue.getKey()))
              .findFirst().orElse(null);
      if (sonarIssue == null) processing = false;
    }

    private void getDescriptionFromFetchedRules() {
      sonarRules = SonarRules.getInstance(project).orElse(null);
      if (sonarRules != null) {
        final SonarRules rules = sonarRules.getState();
        if (rules != null) {
          final Rule rule = rules.getSonarRulesByRuleKey().get(sonarIssue.getRuleKey());
          getDescriptionFromRule(rule);
        }
      }
    }

    private void getDescriptionFromRule(Rule rule) {
      if (rule != null && rule.isValid()) {
        description = rule.getHtmlDesc();
        if (description != null) {
          processing = false;
        }
      }
    }

    private void getSettings() {
      final Module moduleForFile = ModuleUtil.findModuleForFile(psiFile.getVirtualFile(), project);
      if (moduleForFile != null) {
        settings = ModuleSettings.getInstance(moduleForFile).getState();
        if (settings != null) settings = settings.enrichWithProjectSettings(project);
      } else {
        settings = ProjectSettings.getInstance(project).getState();
      }
      if (settings == null) processing = false;
    }

    private void getServerConfig() {
      final String serverName = settings.getServerName();
      serverConfig = SonarServers.get(serverName).orElse(null);
      if (serverConfig == null) processing = false;
    }


    private void persistRule() {
      final SonarServer sonarServer = SonarServer.create(serverConfig);
      persistedRule = sonarServer.getRule(sonarIssue.getRuleKey());
      if (sonarRules != null) {
        final SonarRules sonarRulesState = sonarRules.getState();
        if (sonarRulesState != null && persistedRule.isValid()) {
          sonarRulesState.getSonarRulesByRuleKey().put(sonarIssue.getRuleKey(), persistedRule);
        }
      }
    }

    private void getDescriptionFromPersistedRule() {
      description = persistedRule.getHtmlDesc();
    }

    String getDescription() {
      return description;
    }
  }
}
