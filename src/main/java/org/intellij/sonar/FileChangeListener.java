package org.intellij.sonar;

import com.google.common.base.Optional;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import org.intellij.sonar.analysis.IncrementalScriptProcess;
import org.intellij.sonar.persistence.ChangedFilesComponent;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import static com.google.common.base.Optional.fromNullable;

public class FileChangeListener extends AbstractProjectComponent {

  protected FileChangeListener(final Project project) {
    super(project);

    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {

      @Override
      /**
       * // executes on every small change in the editor
       */
      public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
        //TODO: deactivated
        if (true) return;

        final Optional<PsiFile> psiFile = fromNullable(event.getFile());
        if (!psiFile.isPresent() || !psiFile.get().isValid() || !psiFile.get().isPhysical()) return;
// TODO: console.debug
//        console.info(String.format("Fired before file change for %s", psiFile.get().getName()));

        final Optional<VirtualFile> virtualFile = fromNullable(psiFile.get().getVirtualFile());
        if (!virtualFile.isPresent() || virtualFile.get().isDirectory() || !virtualFile.get().isValid() || !virtualFile.get().isInLocalFileSystem()) return;

        // exclude intellij system files
        if ("workspace.xml".equals(psiFile.get().getName())
            || psiFile.get().getName().endsWith(".iml")
            || psiFile.get().getName().endsWith(".ipr")
            || psiFile.get().getName().endsWith(".iml")
            || psiFile.get().getName().endsWith(".iws")
            || virtualFile.get().getPath().contains("plugins-sandbox")
            || virtualFile.get().getPath().contains(".idea")
            || virtualFile.get().getPath().contains("/target/")
            || virtualFile.get().getPath().contains("/out/")
            ) {
          return;
        }

        // workaround: don't call if FileSaveListener was recently called
        // sometimes if call "Save All" a FileChangeListener event is immediately called
        if (DateTime.now().getMillis() - FileSaveListener.lastFired.getMillis() <= 100) {
// TODO: console.debug
//          console.info(String.format("%s avoided", FileChangeListener.class.getSimpleName()));
          return;
        }


        project.getComponent(ChangedFilesComponent.class).add(psiFile.get());

        stopAllRunningProcesses();
      }
    });
  }

  private void stopAllRunningProcesses() {
    for(IncrementalScriptProcess process: IncrementalScriptProcess.getAllRunningProcesses()) {
      process.kill();
    }
  }
}
