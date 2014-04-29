package org.intellij.sonar.analysis;

import com.intellij.codeInsight.daemon.impl.PsiChangeHandler;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import org.jetbrains.annotations.NotNull;

public class VirtualFileChangesListener extends AbstractProjectComponent {
  protected VirtualFileChangesListener(final Project project) {
    super(project);
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      @Override
      public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
        super.beforeChildAddition(event);
      }

      @Override
      public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {
        super.beforeChildReplacement(event);
      }

      @Override
      public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
        super.beforeChildMovement(event);
      }

      @Override
      public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
        super.beforeChildrenChange(event);

      }

      @Override
      public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {
        super.beforePropertyChange(event);
      }
    });

  }
}
