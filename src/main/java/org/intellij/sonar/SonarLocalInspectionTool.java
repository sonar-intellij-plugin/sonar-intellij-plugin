package org.intellij.sonar;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.intellij.sonar.sonarserver.SonarService;
import org.sonar.wsclient.services.Rule;
import org.sonar.wsclient.services.Violation;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

//import com.intellij.psi.PsiJavaFile;

/**
 * User: Oleg Mayevskiy
 * Date: 23.01.13
 * Time: 10:50
 */
public abstract class SonarLocalInspectionTool extends LocalInspectionTool {

  private SonarService sonarService;

  protected SonarLocalInspectionTool() {
    sonarService = ServiceManager.getService(SonarService.class);
  }

  protected SonarLocalInspectionTool(SonarService sonarService) {
    this.sonarService = sonarService;
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return "Sonar";
  }

  @NotNull
  @Override
  public String getShortName() {
    return this.getDisplayName().replaceAll("[^a-zA-Z_0-9.-]", "");
  }

  @Nls
  @NotNull
  @Override
  abstract public String getDisplayName();

  @NotNull
  @Override
  abstract public String getStaticDescription();

  @NotNull
  abstract public String getRuleKey();

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nullable
  private Map<String, Collection<Violation>> getViolationsMapFromPsiFile(PsiFile file) {
    Map<String, Collection<Violation>> violationsMap = null;
    if (null != file) {
      Project project = file.getProject();
      SonarViolationsProvider sonarViolationsProvider = ServiceManager.getService(project, SonarViolationsProvider.class);
      if (null != sonarViolationsProvider) {
        violationsMap = sonarViolationsProvider.mySonarViolations;
      }
    }
    return violationsMap;
  }

  public Collection<String> getSonarKeyCandidatesForPsiFile(@NotNull PsiFile file, @NotNull String sonarProjectKey) {
    Collection<String> sonarKeyCandidates = new LinkedHashSet<String>();

    String genericSonarKeyCandidate = createGenericSonarKeyCandidate(file, sonarProjectKey);
    sonarKeyCandidates.add(genericSonarKeyCandidate);

    String javaSonarKeyCandidate = createJavaSonarKeyCandidate(file, sonarProjectKey);
    sonarKeyCandidates.add(javaSonarKeyCandidate);

    String phpSonarKeyCandidate = createPhpSonarKeyCandidate(file, sonarProjectKey);
    sonarKeyCandidates.add(phpSonarKeyCandidate);
    return sonarKeyCandidates;
  }

  private String createJavaSonarKeyCandidate(PsiFile file, String sonarProjectKey) {
    String result = createGenericSonarKeyCandidate(file, sonarProjectKey);
    result = result.replace("[root]", "[default]");
    result = result.replaceAll("\\.java$", "");
    result = result.replace("/", ".");

    return result;
  }

  private String createPhpSonarKeyCandidate(PsiFile file, String sonarProjectKey) {
    String result = createGenericSonarKeyCandidate(file, sonarProjectKey);
    result = result.replace("[root]/", "");

    return result;
  }

  private String createGenericSonarKeyCandidate(PsiFile file, String sonarProjectKey) {
    final StringBuilder result = new StringBuilder();
    result.append(sonarProjectKey).append(":");
    final VirtualFile virtualFile = file.getVirtualFile();
    if (null != virtualFile) {
      final String filePath = virtualFile.getPath();

      VirtualFile sourceRootForFile = ProjectFileIndex.SERVICE.getInstance(file.getProject()).getSourceRootForFile(virtualFile);
      // getSourceRootForFile doesn't work in phpstorm for some reasons
      if (null == sourceRootForFile)
        sourceRootForFile = ProjectFileIndex.SERVICE.getInstance(file.getProject()).getContentRootForFile(virtualFile);

      if (null != sourceRootForFile) {
        final String sourceRootForFilePath = sourceRootForFile.getPath() + "/";

        String baseFileName = filePath.replace(sourceRootForFilePath, "");

        if (baseFileName.equals(file.getName())) {
          result.append("[root]/");
        }

        result.append(baseFileName);
      }
    }
    return result.toString();
  }

  private SonarSettingsBean getSonarSettingsBeanForFile(PsiFile file) {
    SonarSettingsBean sonarSettingsBean = null;
    if (null != file) {
      VirtualFile virtualFile = file.getVirtualFile();
      if (null != virtualFile) {
        Module module = ModuleUtil.findModuleForFile(virtualFile, file.getProject());
        if (null != module) {
          SonarSettingsComponent component = module.getComponent(SonarSettingsModuleComponent.class);
          SonarSettingsBean moduleSonarSettingsBean = getSonarSettingsBeanFromSonarComponent(component);
          if (null != moduleSonarSettingsBean && !moduleSonarSettingsBean.isEmpty())
            sonarSettingsBean = moduleSonarSettingsBean;
        }
      }
      if (null == sonarSettingsBean) {
        sonarSettingsBean = getSonarSettingsBeanFromProject(file.getProject());
      }
    }

    return sonarSettingsBean;
  }

  @NotNull
  private TextRange getTextRange(@NotNull Document document, int line) {
    int lineStartOffset = document.getLineStartOffset(line - 1);
    int lineEndOffset = document.getLineEndOffset(line - 1);
    return new TextRange(lineStartOffset, lineEndOffset);
  }

  @Nullable
  @Override
  public ProblemDescriptor[] checkFile(@NotNull final PsiFile psiFile, @NotNull final InspectionManager manager, final boolean isOnTheFly) {
    // don't care about non physical files
    if (psiFile.getVirtualFile() == null || ProjectFileIndex.SERVICE.getInstance(psiFile.getProject()).getSourceRootForFile(psiFile.getVirtualFile()) == null) {
      return null;
    }
    final Project project = psiFile.getProject();
    final Collection<ProblemDescriptor> result = new LinkedHashSet<ProblemDescriptor>();
    final SonarSettingsBean sonarSettingsBean = getSonarSettingsBeanForFile(psiFile);
    PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());
    Document document = documentManager.getDocument(psiFile.getContainingFile());
    if (document == null) {
      return null;
    }

    if (null != sonarSettingsBean) {
      Collection<String> sonarKeyCandidates = getSonarKeyCandidatesForPsiFile(psiFile, sonarSettingsBean.resource);
      Map<String, Collection<Violation>> violationsMapFromPsiFile = getViolationsMapFromPsiFile(psiFile);

      String sonarKey = getFirstSonarKeyCandidateForFileToViolationsMap(sonarKeyCandidates, violationsMapFromPsiFile);
      Collection<Violation> violations = null != sonarKey && null != violationsMapFromPsiFile ? violationsMapFromPsiFile.get(sonarKey) : null;

      if (null != violations) {
        for (Violation violation : violations) {
          // add only violations of this rule
          if (this.getRuleKey().equals(violation.getRuleKey())) {
            TextRange textRange = getTextRange(document, violation.getLine());
            ProblemHighlightType problemHighlightType = getProblemHighlightTypeForRuleKey(project, violation.getRuleKey());

            result.add(manager.createProblemDescriptor(psiFile, textRange,
                violation.getMessage(),
                problemHighlightType,
                false
            ));
          }
        }
      }
    }

    return result.toArray(new ProblemDescriptor[result.size()]);
  }

  @Nullable
  private String getFirstSonarKeyCandidateForFileToViolationsMap(Collection<String> sonarKeyCandidates, Map<String, Collection<Violation>> violationsMapFromPsiFile) {
    if (null != sonarKeyCandidates && null != violationsMapFromPsiFile) {
      for (String sonarKeyCandidate : sonarKeyCandidates) {
        if (violationsMapFromPsiFile.containsKey(sonarKeyCandidate)) {
          return sonarKeyCandidate;
        }
      }
    }
    return null;
  }

  private ProblemHighlightType getProblemHighlightTypeForRuleKey(Project project, String ruleKey) {
    SonarRulesProvider sonarRulesProvider = ServiceManager.getService(project, SonarRulesProvider.class);
    if (null != sonarRulesProvider) {
      SonarRulesProvider sonarRulesProviderState = sonarRulesProvider.getState();
      if (null != sonarRulesProviderState) {
        Rule rule = sonarRulesProviderState.sonarRulesByRuleKey.get(ruleKey);
        if (null != rule) {
          return sonarService.sonarSeverityToProblemHighlightType(rule.getSeverity());
        }
      }
    }
    return ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
  }

  private SonarSettingsBean getSonarSettingsBeanFromProject(Project project) {
    SonarSettingsProjectComponent sonarProjectComponent = project.getComponent(SonarSettingsProjectComponent.class);
    return sonarProjectComponent.getState();
  }

  private SonarSettingsBean getSonarSettingsBeanFromSonarComponent(SonarSettingsComponent sonarSettingsComponent) {
    return sonarSettingsComponent.getState();
  }

  @Nullable
  private PsiElement getElementAtLine(@NotNull PsiFile file, int line) {
    PsiElement element = null;
    final Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
    if (null != document) {
      int offset = document.getLineStartOffset(line);
      element = file.getViewProvider().findElementAt(offset);
      if (null != element && document.getLineNumber(element.getTextOffset()) != line) {
        element = element.getNextSibling();
      }
    }
    return element;
  }

}
