package org.intellij.sonar;

import com.google.common.collect.Sets;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginId;
import org.intellij.sonar.analysis.SonarExternalAnnotator;

import java.util.Set;
import java.util.stream.Collectors;

public class SonarIntellijPluginApplicationService {

    // registered languages does not contain all languages
    // even if the lange is supported by the IDE
    // therefore loading of the languages from bellow is forced
    private static final Set<String> PRE_DEFINED_LANGUAGES = Sets.newHashSet(
            "JAVA",
            "kotlin",
            "Python",
            "PHP",
            "JavaScript",
            "ruby",
            "XML",
            "Scala",
            "Swift"
    );
    private final IdeaPluginDescriptor plugin;

    public SonarIntellijPluginApplicationService() {
        plugin = PluginManagerCore.getPlugin(PluginId.getId("org.mayevskiy.intellij.sonar"));
        if (plugin != null && plugin.isEnabled()) {
            registerExternalAnnotatorForAllLanguages();
        }
    }

    private void registerExternalAnnotatorForAllLanguages() {
        // filters fix #212: displaying annotations three times
        PRE_DEFINED_LANGUAGES.forEach(this::registerExternalAnnotator);
        Language.getRegisteredLanguages()
                .stream()
                .filter(SonarIntellijPluginApplicationService::doesNotImplementMetaLanguage)
                .filter(SonarIntellijPluginApplicationService::doesNotHaveBaseLanguage)
                .filter(SonarIntellijPluginApplicationService::isNotPredefined)
                .forEach(this::registerExternalAnnotator);
    }

    private static boolean isNotPredefined(Language language) {
        return !PRE_DEFINED_LANGUAGES
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet())
                .contains(language.getID().toLowerCase());
    }

    private static boolean doesNotImplementMetaLanguage(Language lang) {
        Class<?> superclass = lang.getClass().getSuperclass();
        while (superclass != null) {
            if ("com.intellij.lang.MetaLanguage".equals(superclass.getName())) {
                return false;
            }
            superclass = superclass.getSuperclass();
        }
        return true;
    }

    private static boolean doesNotHaveBaseLanguage(Language lang) {
        return lang.getBaseLanguage() == null;
    }

    private void registerExternalAnnotator(Language language) {
        registerExternalAnnotator(language.getID());
    }

    private void registerExternalAnnotator(String languageId) {
        LanguageExtensionPoint<SonarExternalAnnotator> ep = new LanguageExtensionPoint<>(
                languageId,
                SonarExternalAnnotator.class.getName(),
                plugin);

        Extensions
                .getRootArea()
                .getExtensionPoint("com.intellij.externalAnnotator")
                .registerExtension(ep);
    }

}
