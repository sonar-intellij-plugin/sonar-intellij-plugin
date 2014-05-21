package org.intellij.sonar.index;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IssuesIndex {
  private static final long serialVersionUID = -363823551203722283L;
  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> indexValue = new ConcurrentHashMap<IssuesIndexKey, Set<IssuesIndexEntry>>();

  @SuppressWarnings("UnusedDeclaration")
  // is used by intellij serializer
  public IssuesIndex() {
  }

  public IssuesIndex(Map<IssuesIndexKey, ? extends Set<IssuesIndexEntry>> indexValue) {
    this.indexValue = (Map<IssuesIndexKey, Set<IssuesIndexEntry>>) indexValue;
  }

  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> getIndexValue() {
    return indexValue;
  }

  public void setIndexValue(Map<IssuesIndexKey, Set<IssuesIndexEntry>> indexValue) {
    this.indexValue = indexValue;
  }



}
