package org.intellij.sonar;

import com.google.common.base.Optional;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBusConnection;
import org.intellij.sonar.inspection.SonarLocalInspectionTool;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import static com.google.common.base.Optional.fromNullable;

public class FileEditorListener extends AbstractProjectComponent {
  private final static Logger LOG = Logger.getInstance(FileEditorListener.class);

  protected FileEditorListener(final Project project) {
    super(project);

    MessageBusConnection msgBus = project.getMessageBus().connect(project);
    msgBus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
        new FileEditorManagerAdapter() {
          @Override
          public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            final VirtualFile virtualFile = event.getNewFile();
            if (null != virtualFile) {
              refreshInspectionsInEditorIfNeededFor(virtualFile);
            }
          }

          @Override
          public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile virtualFile) {
            refreshInspectionsInEditorIfNeededFor(virtualFile);
          }

          private void refreshInspectionsInEditorIfNeededFor(VirtualFile file) {
            final Optional<PsiFile> psiFile = fromNullable(PsiManager.getInstance(project).findFile(file));
            if (psiFile.isPresent() && !FileChangeListener.changedPsiFiles.contains(psiFile.get())) {
              SonarLocalInspectionTool.refreshInspectionsInEditor(project);
            }
          }

        }
    );
  }
}
