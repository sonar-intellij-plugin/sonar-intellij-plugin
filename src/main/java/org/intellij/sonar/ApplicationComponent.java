package org.intellij.sonar;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.PluginId;
import org.intellij.sonar.analysis.SonarExternalAnnotator;
import org.jetbrains.annotations.NotNull;

public class ApplicationComponent implements com.intellij.openapi.components.ApplicationComponent {

  private IdeaPluginDescriptor plugin;

  @Override
  public void initComponent() {
    plugin = PluginManager.getPlugin(PluginId.getId("org.mayevskiy.intellij.sonar"));
    if (plugin != null && plugin.isEnabled()) {
      registerExternalAnnotatorForAllLanguages();
    }
  }

  private void registerExternalAnnotatorForAllLanguages() {
    for (Language language : Language.getRegisteredLanguages()) {
      registerExternalAnnotatorFor(language);
    }
  }

  private void registerExternalAnnotatorFor(Language language) {
    LanguageExtensionPoint<SonarExternalAnnotator> ep = new LanguageExtensionPoint<SonarExternalAnnotator>();
    ep.language = language.getID();
    ep.implementationClass = SonarExternalAnnotator.class.getName();
    ep.setPluginDescriptor(plugin);
    Extensions.getRootArea().getExtensionPoint("com.intellij.externalAnnotator").registerExtension(ep);
  }

  @Override
  public void disposeComponent() {
    // nothing to free
  }

  @NotNull
  @Override
  public String getComponentName() {
    return getClass().getSimpleName();
  }
}
