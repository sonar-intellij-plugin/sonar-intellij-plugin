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
import org.intellij.sonar.analysis.IncrementalScriptProcess;
import org.intellij.sonar.console.SonarConsole;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.Set;

import static com.google.common.base.Optional.fromNullable;

public class FileChangeListener extends AbstractProjectComponent {

  public final static Set<PsiFile> changedPsiFiles = new ConcurrentHashSet<PsiFile>();
  private final SonarConsole console;

  protected FileChangeListener(Project project) {
    super(project);
    console = SonarConsole.get(project);
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {

      @Override
      /**
       * // executes on every small change in the editor
       */
      public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

        final Optional<PsiFile> psiFile = fromNullable(event.getFile());
        if (!psiFile.isPresent()) return;

        console.info(String.format("Fired before file change for %s", psiFile.get().getName()));

        final Optional<VirtualFile> virtualFile = fromNullable(psiFile.get().getVirtualFile());
        if (!virtualFile.isPresent() || virtualFile.get().isDirectory()) return;

        // exclude intellij system files
        if ("workspace.xml".equals(psiFile.get().getName())
            || psiFile.get().getName().endsWith(".iml")
            || psiFile.get().getName().endsWith(".ipr")
            || psiFile.get().getName().endsWith(".iml")
            || psiFile.get().getName().endsWith(".iws")
            || virtualFile.get().getPath().contains("plugins-sandbox")
            || virtualFile.get().getPath().contains(".idea")
            ) {
          return;
        }

        // workaround: don't call if FileSaveListener was recently called
        // sometimes if call "Save All" a FileChangeListener event is immediately called
        if (DateTime.now().getMillis() - FileSaveListener.lastFired.getMillis() <= 300) {
          console.info(String.format("%s avoided", FileChangeListener.class.getSimpleName()));
          return;
        }

        console.info(String.format("stop all processes for %s", psiFile.get().getName()));
        changedPsiFiles.add(psiFile.get());

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
