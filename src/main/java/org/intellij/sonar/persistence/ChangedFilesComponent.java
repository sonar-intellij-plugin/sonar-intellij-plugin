package org.intellij.sonar.persistence;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@State(
    name = "changed-files-component",
    storages = {
        @Storage(id = "changed-files-component", file = BaseDir.PATH + "changed-files.xml")
    }
)
public class ChangedFilesComponent implements PersistentStateComponent<ChangedFilesComponent>, ProjectComponent {
  public Set<String> changedFiles = new ConcurrentHashSet<String>();

  @Transient
  public void add(PsiFile psiFile) {
    final String path = psiFile.getVirtualFile().getPath();
    changedFiles.add(path);
  }

  @Transient
  public ImmutableSet<PsiFile> getPsiFiles() {
    final ImmutableSet<PsiFile> psiFiles = FluentIterable.from(changedFiles)
        .transform(new Function<String, VirtualFile>() {
          @Override
          public VirtualFile apply(String path) {
            return LocalFileSystem.getInstance().findFileByPath(path);
          }
        }).filter(new Predicate<VirtualFile>() {
          @Override
          public boolean apply(VirtualFile virtualFile) {
            return null != virtualFile;
          }
        })
        .transform(new Function<VirtualFile, Pair<VirtualFile, Project>>() {
          @Override
          public Pair<VirtualFile, Project> apply(VirtualFile virtualFile) {
            return new Pair<VirtualFile, Project>(virtualFile, ProjectUtil.guessProjectForFile(virtualFile));
          }
        })
        .filter(new Predicate<Pair<VirtualFile, Project>>() {
          @Override
          public boolean apply(Pair<VirtualFile, Project> virtualFileProjectPair) {
            return null != virtualFileProjectPair.getSecond();
          }
        })
        .transform(new Function<Pair<VirtualFile, Project>, PsiFile>() {
          @Override
          public PsiFile apply(Pair<VirtualFile, Project> virtualFileProjectPair) {
            return PsiManager.getInstance(virtualFileProjectPair.getSecond()).findFile(virtualFileProjectPair.first);
          }
        })
        .filter(new Predicate<PsiFile>() {
          @Override
          public boolean apply(PsiFile psiFile) {
            return null != psiFile;
          }
        }).toSet();

    return psiFiles;
  }

  @Nullable
  @Override
  public ChangedFilesComponent getState() {
    return this;
  }

  @Override
  public void loadState(ChangedFilesComponent state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  /**
   * Invoked when the project corresponding to this component instance is opened.<p>
   * Note that components may be created for even unopened projects and this method can be never
   * invoked for a particular component instance (for example for default project).
   */
  @Override
  public void projectOpened() {

  }

  /**
   * Invoked when the project corresponding to this component instance is closed.<p>
   * Note that components may be created for even unopened projects and this method can be never
   * invoked for a particular component instance (for example for default project).
   */
  @Override
  public void projectClosed() {

  }

  /**
   * Component should perform initialization and communication with other components in this method.
   */
  @Override
  public void initComponent() {

  }

  /**
   * Component should dispose system resources or perform other cleanup in this method.
   */
  @Override
  public void disposeComponent() {

  }

  /**
   * Unique name of this component. If there is another component with the same name or
   * name is null internal assertion will occur.
   *
   * @return the name of this component
   */
  @NotNull
  @Override
  public String getComponentName() {
    return this.getClass().getSimpleName();
  }
}
