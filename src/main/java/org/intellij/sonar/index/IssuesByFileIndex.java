package org.intellij.sonar.index;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.intellij.sonar.persistence.IssuesByFileIndexProjectComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class IssuesByFileIndex {

  public static Map<String, Set<SonarIssue>> getIndex(Project project) {

    final Optional<IssuesByFileIndexProjectComponent> indexComponent = IssuesByFileIndexProjectComponent.getInstance(project);
    if (!indexComponent.isPresent()) {
      return Maps.newConcurrentMap();
    } else {
      return indexComponent.get().getIndex();
    }
  }

  public static Set<SonarIssue> getIssuesForFile(PsiFile psiFile) {
    String fullPath = psiFile.getVirtualFile().getPath();
    Project project = psiFile.getProject();
    final Map<String, Set<SonarIssue>> index = getIndex(project);
    Set<SonarIssue> issues = index.get(fullPath);
    if (issues == null) {
      issues = Sets.newLinkedHashSet();
    }
    return issues;
  }

  public static void clearIndexFor(Collection<PsiFile> psiFiles) {
    for (PsiFile psiFile : psiFiles) {
      getIndex(psiFile.getProject()).remove(psiFile.getVirtualFile().getPath());
    }
  }
}
