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

    @Override
    public void initComponent() {
        IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("org.mayevskiy.intellij.sonar"));
        if (plugin != null && plugin.isEnabled()) {
            registerExternalAnnotatorForAllLanguages(plugin);
        }
    }

    private void registerExternalAnnotatorForAllLanguages(IdeaPluginDescriptor plugin) {
        for (Language language : Language.getRegisteredLanguages()) {
            LanguageExtensionPoint<SonarExternalAnnotator> extensionPoint = new LanguageExtensionPoint<SonarExternalAnnotator>();
            extensionPoint.language = language.getID();
            extensionPoint.implementationClass = SonarExternalAnnotator.class.getName();
            extensionPoint.setPluginDescriptor(plugin);
            Extensions.getRootArea().getExtensionPoint("com.intellij.externalAnnotator").registerExtension(extensionPoint);
        }
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getSimpleName();
    }
}
