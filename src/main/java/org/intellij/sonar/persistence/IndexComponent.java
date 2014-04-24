package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.intellij.sonar.index.Index;
import org.jetbrains.annotations.Nullable;

@State(
    name = "index-component",
    storages = {
        @Storage(id = "index-component", file = BaseDir.PATH + "sonar-index.xml")
    }
)
public class IndexComponent implements PersistentStateComponent<Index> {

  private Index index;

  @Nullable
  @Override
  public Index getState() {
    return index;
  }

  @Override
  public void loadState(Index state) {
    index = state;
  }
}
