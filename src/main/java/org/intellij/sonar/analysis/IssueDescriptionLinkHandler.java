package org.intellij.sonar.analysis;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.intellij.codeInsight.highlighting.TooltipLinkHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.index2.SonarIssue;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.sonarserver.Rule;
import org.intellij.sonar.sonarserver.SonarServer;
import org.intellij.sonar.util.SettingsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class IssueDescriptionLinkHandler extends TooltipLinkHandler {

  @Nullable
  @Override
  public String getDescription(@NotNull final String sonarIssueKey, @NotNull final Editor editor) {
    // retrieve sonar issue data
    final Project project = editor.getProject();
    if (project == null || project.isDisposed()) return null;
    final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
    if (psiFile == null) return null;
    final Optional<IssuesByFileIndexProjectComponent> indexComponent = IssuesByFileIndexProjectComponent.getInstance(project);
    if (!indexComponent.isPresent()) return null;
    final IssuesByFileIndexProjectComponent state = indexComponent.get().getState();
    if (state == null) return null;
    final Map<String, Set<SonarIssue>> index = state.getIndex();
    final String path = psiFile.getVirtualFile().getPath();
    final Set<SonarIssue> issues = index.get(path);
    final Optional<SonarIssue> issue = FluentIterable.from(issues).firstMatch(new Predicate<SonarIssue>() {
      @Override
      public boolean apply(SonarIssue sonarIssue) {
        return sonarIssueKey.equals(sonarIssue.getKey());
      }
    });
    if (!issue.isPresent()) return null;

    // try to get desc from already fetched rules
    final Optional<SonarRules> sonarRules = SonarRules.getInstance(project);
    if (sonarRules.isPresent()) {
      final SonarRules sonarRulesState = sonarRules.get().getState();
      if (sonarRulesState != null) {
        final Rule rule = sonarRulesState.getSonarRulesByRuleKey().get(issue.get().getRuleKey());
        if (rule != null && !rule.isEmpty()) return rule.getDesc();
      }
    }

    // fetch and persist rule
    Settings settings;
    final Module moduleForFile = ModuleUtil.findModuleForFile(psiFile.getVirtualFile(), project);
    if (moduleForFile != null) {
      settings = ModuleSettings.getInstance(moduleForFile).getState();
      settings = SettingsUtil.process(project, settings);
    } else {
      settings = ProjectSettings.getInstance(project).getState();
    }
    if (settings == null) return null;

    final String serverName = settings.getServerName();
    final Optional<SonarServerConfig> serverConfig = SonarServers.get(serverName);
    if (!serverConfig.isPresent()) return null;
    final SonarServer sonarServer = SonarServer.create(serverConfig.get());
    final Rule rule = sonarServer.getRule(issue.get().getRuleKey());

    // persist rule
    if (sonarRules.isPresent()) {
      final SonarRules sonarRulesState = sonarRules.get().getState();
      if (sonarRulesState!= null && !rule.isEmpty()) {
        sonarRulesState.getSonarRulesByRuleKey().put(issue.get().getRuleKey(), rule);
      }
    }

    return rule.getDesc();
  }
}
