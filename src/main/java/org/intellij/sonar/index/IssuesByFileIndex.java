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
  public static final Map<String, Set<SonarIssue>> index = Maps.newLinkedHashMap();

  public static Map<String, Set<SonarIssue>> getIndex(Project project) {
//     return getFakeIndex();

    final Optional<IssuesByFileIndexProjectComponent> indexComponent = IssuesByFileIndexProjectComponent.getInstance(project);
    if (!indexComponent.isPresent()) {
      return Maps.newConcurrentMap();
    } else {
      return indexComponent.get().getState().getIndex();
    }
  }

  /*// TODO: remove after real index is done
  public static Map<String, Set<SonarIssue>> getFakeIndex() {
    Set<SonarIssue> issues = Sets.newLinkedHashSet();
    issues.add(new SonarIssue(11, "blocker issue", SonarSeverity.BLOCKER.toString(), true));
    issues.add(new SonarIssue(12, "info issue", SonarSeverity.INFO.toString(), false));
    issues.add(new SonarIssue(13, "major issue", SonarSeverity.MAJOR.toString(), true));
    issues.add(new SonarIssue(10, "BLOCKER issue", SonarSeverity.MAJOR.toString(), false));
    issues.add(new SonarIssue(10, "MAJOR issue", SonarSeverity.MAJOR.toString(), true));
    issues.add(new SonarIssue(10, "INFO issue", SonarSeverity.MAJOR.toString(), false));
    issues.add(new SonarIssue(null, "file level issue", SonarSeverity.MAJOR.toString(), true));
    index.put(
        "/Users/omayevskiy/workspace/SonarSource-sonar-examples-4a4a681/projects/languages/java/maven/java-maven-simple/src/main/java/example/One.java",
        issues
    );
    return index;
  }*/

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
      index.remove(psiFile.getVirtualFile().getPath());
    }
  }
}
