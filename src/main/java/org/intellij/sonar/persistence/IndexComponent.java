package org.intellij.sonar.persistence;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.intellij.sonar.index.IssuesIndex;
import org.intellij.sonar.index.IssuesIndexEntry;
import org.intellij.sonar.index.IssuesIndexKey;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = "index-component",
    storages = {
        @Storage(id = "index-component" + IndexComponent.serialVersionUID , file = BaseDir.PATH + "index-component.xml")
    }
)
public class IndexComponent implements PersistentStateComponent<IndexComponent>, Serializable {

  public static final long serialVersionUID = -2073972896207461743L;
  private Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex;

  public IndexComponent() {
    issuesIndex = new ConcurrentHashMap<IssuesIndexKey, Set<IssuesIndexEntry>>();
  }

  @Nullable
  @Override
  public IndexComponent getState() {
    return this;
  }

  @Override
  public void loadState(IndexComponent state) {
    XmlSerializerUtil.copyBean(state, this);
  }

  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> getIssuesIndex() {
    return issuesIndex;
  }

  public void setIssuesIndex(Map<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndex) {
    this.issuesIndex = issuesIndex;
  }
}
