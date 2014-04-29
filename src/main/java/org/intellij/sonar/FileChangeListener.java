package org.intellij.sonar;

import com.google.common.base.Optional;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.util.containers.ConcurrentHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.google.common.base.Optional.fromNullable;

public class FileChangeListener extends AbstractProjectComponent {

  public static volatile boolean isBlocked = false;
  public final static Set<PsiFile> changedPsiFiles = new ConcurrentHashSet<PsiFile>();

  protected FileChangeListener(Project project) {
    super(project);
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {

      @Override
      public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

        if (isBlocked) return;

        final Optional<PsiFile> psiFile = fromNullable(event.getFile());
        if (!psiFile.isPresent()) return;

        final Optional<VirtualFile> virtualFile = fromNullable(psiFile.get().getVirtualFile());
        if (!virtualFile.isPresent() || virtualFile.get().isDirectory()) return;

        changedPsiFiles.add(psiFile.get());
      }
    });
  }
}
