package org.intellij.sonar.action;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import org.intellij.sonar.persistence.*;
import org.intellij.sonar.sonarserver.SonarServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.wsclient.services.Profile;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.Rule;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadQualityProfileAction extends AnAction {
  private final static Logger LOG = Logger.getInstance(DownloadQualityProfileAction.class);

  private class BackgroundTask extends Task.Backgroundable {

    private BackgroundTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled, @Nullable PerformInBackgroundOption backgroundOption) {
      super(project, title, canBeCancelled, backgroundOption);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
      doAction(getProject());
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Optional<Project> project = Optional.fromNullable(e.getData(DataKeys.PROJECT));
    if (!project.isPresent()) {
      LOG.error(String.format("project not available for the %s", DownloadQualityProfileAction.class.getSimpleName()));
      return;
    }

    new BackgroundTask(project.get(), "Download Quality Profile", true, PerformInBackgroundOption.ALWAYS_BACKGROUND)
        .queue();
  }

  private void doAction(Project project) {
    final ImmutableSet.Builder<SonarRuleBean> allSonarRuleBeansBuilder = ImmutableSet.builder();

    // handle project
    final Optional<ProjectSettingsComponent> projectComponent = Optional.fromNullable(project.getComponent(ProjectSettingsComponent.class));
    if (!projectComponent.isPresent()) {
      LOG.error(String.format("%s not available for the %s", ProjectSettingsComponent.class.getSimpleName(), DownloadQualityProfileAction.class.getSimpleName()));
      return;
    }

    final Optional<ProjectSettingsBean> projectComponentState = Optional.fromNullable(projectComponent.get().getState());
    if (!projectComponentState.isPresent()) {
      LOG.error(String.format("No %s found for the %s", ProjectSettingsBean.class.getSimpleName(), DownloadQualityProfileAction.class.getSimpleName()));
      return;
    }
    final ProjectSettingsBean projectSettingsBean = projectComponentState.get();
    final Optional<String> sonarServerName = projectSettingsBean.getProperSonarServerName();

    final Map<String, ImmutableSet<Rule>> allRulesForLanguageCache = new ConcurrentHashMap<String, ImmutableSet<Rule>>();
    final Map<Pair<String, String>, ImmutableSet<Profile.Rule>> rulesOfQualityProfileCache = new ConcurrentHashMap<Pair<String, String>, ImmutableSet<Profile.Rule>>();

    if (sonarServerName.isPresent()) {
      final Optional<SonarServerConfigurationBean> sonarServerConfigurationBean = SonarServersComponent.get(sonarServerName.get());
      if (sonarServerConfigurationBean.isPresent()) {
        final SonarServer sonarServer = SonarServer.create(sonarServerConfigurationBean.get());
        final Collection<Resource> resources = projectSettingsBean.getResources();
        if (resources.size() > 0 ) {
          allSonarRuleBeansBuilder.addAll(
              collectRulesFor(allRulesForLanguageCache, rulesOfQualityProfileCache, sonarServer, resources)
          );
        }
      }
    }

    // handle modules
    final Optional<Module[]> modules = Optional.fromNullable(ModuleManager.getInstance(project).getModules());
    if (!modules.isPresent()) {
      return;
    }
    for (Module module : modules.get()) {
      final Optional<ModuleSettingsComponent> moduleComponent = Optional.fromNullable(module.getComponent(ModuleSettingsComponent.class));
      if (moduleComponent.isPresent()) {
        final Optional<ModuleSettingsBean> moduleSettingsState = Optional.fromNullable(moduleComponent.get().getState());
        if (moduleSettingsState.isPresent()) {
          final Optional<String> properServerName = moduleSettingsState.get().getProperServerName(project);
          if (properServerName.isPresent()) {
            final Optional<SonarServerConfigurationBean> sonarServerConfigurationBean = SonarServersComponent.get(properServerName.get());
            if (sonarServerConfigurationBean.isPresent()) {
              final SonarServer sonarServer = SonarServer.create(sonarServerConfigurationBean.get());
              final Collection<Resource> resources = moduleSettingsState.get().getResources();
              if (resources.size() > 0 ) {
                allSonarRuleBeansBuilder.addAll(
                    collectRulesFor(allRulesForLanguageCache, rulesOfQualityProfileCache, sonarServer, resources)
                );
              }
            }
          }
        }
      }

    }

    // update rules index
    final ImmutableSet<SonarRuleBean> sonarRuleBeans = allSonarRuleBeansBuilder.build();
    final Optional<SonarRulesComponent> sonarRulesComponent = Optional.fromNullable(ServiceManager.getService(project, SonarRulesComponent.class));
    if (sonarRulesComponent.isPresent()) {
      final Optional<SonarRulesComponent> sonarRulesComponentState = Optional.fromNullable(sonarRulesComponent.get().getState());
      if (sonarRulesComponentState.isPresent()) {
        final Map<String, SonarRuleBean> currentSonarRulesByRuleKey = sonarRulesComponentState.get().getSonarRulesByRuleKey();
        final Map<String, SonarRuleBean> newSonarRulesByRuleKey = new ConcurrentHashMap<String, SonarRuleBean>();
        for (SonarRuleBean sonarRuleBean: sonarRuleBeans) {
          newSonarRulesByRuleKey.put(sonarRuleBean.getKey(), sonarRuleBean);
        }
        if (!equalMaps(currentSonarRulesByRuleKey, newSonarRulesByRuleKey)) {
          sonarRulesComponent.get().setSonarRulesByRuleKey(ImmutableMap.copyOf(newSonarRulesByRuleKey));
          showRestartIdeDialog(currentSonarRulesByRuleKey, newSonarRulesByRuleKey);
        }
      }
    }

  }

  private ImmutableSet<SonarRuleBean> collectRulesFor(Map<String, ImmutableSet<Rule>> allRulesForLanguageCache, Map<Pair<String, String>, ImmutableSet<Profile.Rule>> rulesOfQualityProfileCache, SonarServer sonarServer, Collection<Resource> resources) {
    for (final Resource resource : resources) {
      final Resource resourceWithProfile = sonarServer.getResourceWithProfile(resource.getKey());

      final String language = resourceWithProfile.getLanguage();
      // if not yet loaded all rules for this language
      final ImmutableSet<Rule> allRulesForLanguage;
      final Optional<ImmutableSet<Rule>> allRulesForLanguageFromCache = Optional.fromNullable(allRulesForLanguageCache.get(language));
      if (allRulesForLanguageFromCache.isPresent()) {
        allRulesForLanguage = allRulesForLanguageFromCache.get();
      } else {
        allRulesForLanguage = ImmutableSet.copyOf(sonarServer.getRules(language));
        allRulesForLanguageCache.put(language, allRulesForLanguage);
      }

      final String profile = resourceWithProfile.getMeasure("profile").getData();
      final ImmutableSet<Profile.Rule> rulesOfQualityProfile;
      final Pair<String, String> languageProfilePair = new Pair<String, String>(language, profile);
      final Optional<ImmutableSet<Profile.Rule>> rulesOfQualityProfileFromCache = Optional.fromNullable(rulesOfQualityProfileCache.get(languageProfilePair));
      if (rulesOfQualityProfileFromCache.isPresent()) {
        rulesOfQualityProfile = rulesOfQualityProfileFromCache.get();
      } else {
        rulesOfQualityProfile = ImmutableSet.copyOf(sonarServer.getProfile(language, profile)
            .getRules());
        rulesOfQualityProfileCache.put(languageProfilePair, rulesOfQualityProfile);
      }

      final ImmutableSet<Rule> enrichedQualityProfileRules = FluentIterable.from(allRulesForLanguage)
          .filter(new Predicate<Rule>() {
            @Override
            public boolean apply(final Rule ruleForLanguage) {
              return FluentIterable.from(rulesOfQualityProfile)
                  .anyMatch(new Predicate<Profile.Rule>() {
                    @Override
                    public boolean apply(Profile.Rule ruleOfQualityProfile) {
                      return ruleForLanguage.getKey()
                          .equals(String.format("%s:%s", ruleOfQualityProfile.getRepository(), ruleOfQualityProfile.getKey()));
                    }
                  });
            }
          }).toSet();

      final ImmutableSet<SonarRuleBean> sonarRuleBeans = FluentIterable.from(enrichedQualityProfileRules)
          .transform(new Function<Rule, SonarRuleBean>() {
            @Override
            public SonarRuleBean apply(Rule rule) {
              return new SonarRuleBean(rule.getTitle(), rule.getDescription(), rule.getKey(), rule.getRepository(), rule.getSeverity());
            }
          }).toSet();

      return sonarRuleBeans;

    }
    return ImmutableSet.of();
  }



  /*public int syncWithSonar(Project project, @NotNull ProgressIndicator indicator) {
    *//*Collection<SonarSettingsBean> allSonarSettingsBeans = SonarSettingsComponent.getSonarSettingsBeans(project);
    SonarServer sonarServer = ServiceManager.getService(SonarServer.class);

    Map<String, Rule> sonarRulesByRuleKeyFromServer = new HashMap<String, Rule>();
    for (Rule rule : sonarServer.getAllRules(allSonarSettingsBeans, indicator)) {
      sonarRulesByRuleKeyFromServer.put(rule.getKey(), rule);
    }

    if (!equalMaps(sonarRulesByRuleKey, sonarRulesByRuleKeyFromServer)) {
      sonarRulesByRuleKey.clear();
      sonarRulesByRuleKey.putAll(sonarRulesByRuleKeyFromServer);
      showRestartIdeDialog();
    }
*//*
//    return sonarRulesByRuleKey.size();
  }*/

  private void showRestartIdeDialog(final Map<String, SonarRuleBean> currentSonarRulesByRuleKey, final Map<String, SonarRuleBean> newSonarRulesByRuleKey) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        final int ret = Messages.showOkCancelDialog(
            String.format("Detected new sonar rules (%d new vs. %d current). You have to restart IDE to reload the settings. Restart?",
                newSonarRulesByRuleKey.entrySet().size(), currentSonarRulesByRuleKey.entrySet().size()),
            IdeBundle.message("title.restart.needed"), Messages.getQuestionIcon());
        if (ret == 0) {
          if (ApplicationManager.getApplication().isRestartCapable()) {
            ApplicationManager.getApplication().restart();
          } else {
            ApplicationManager.getApplication().exit();
          }
        }
      }
    });

  }

  private boolean equalMaps(Map<String, SonarRuleBean> m1, Map<String, SonarRuleBean> m2) {
    if (m1.size() != m2.size())
      return false;
    for (String key1 : m1.keySet()) {
      if (!m2.containsKey(key1)) {
        return false;
      }
    }
    return true;
  }
}
