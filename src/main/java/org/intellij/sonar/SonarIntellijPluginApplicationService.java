package org.intellij.sonar;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginId;
import org.intellij.sonar.analysis.SonarExternalAnnotator;

public class SonarIntellijPluginApplicationService {

  private final IdeaPluginDescriptor plugin;

  public SonarIntellijPluginApplicationService() {
    plugin = PluginManagerCore.getPlugin(PluginId.getId("org.mayevskiy.intellij.sonar"));
    if (plugin != null && plugin.isEnabled()) {
      registerExternalAnnotatorForAllLanguages();
    }
  }

  private void registerExternalAnnotatorForAllLanguages() {
    // filters fix #212: displaying annotations three times
    Language.getRegisteredLanguages().stream()
            .filter(SonarIntellijPluginApplicationService::doesNotImplementMetaLanguage)
            .filter(SonarIntellijPluginApplicationService::doesNotHaveBaseLanguage)
            .forEach(this::registerExternalAnnotatorFor);
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

  private void registerExternalAnnotatorFor(Language language) {
    LanguageExtensionPoint<SonarExternalAnnotator> ep = new LanguageExtensionPoint<>(
            language.getID(),
            SonarExternalAnnotator.class.getName(),
            plugin);

    Extensions.getRootArea().getExtensionPoint("com.intellij.externalAnnotator").registerExtension(ep);
  }

}
