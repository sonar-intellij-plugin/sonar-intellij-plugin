package org.intellij.sonar.index;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class IssuesIndexMerge {

  private ImmutableMap<IssuesIndexKey, Set<IssuesIndexEntry>> current;
  private ImmutableMap<IssuesIndexKey, Set<IssuesIndexEntry>> update;

  public IssuesIndexMerge(Map<IssuesIndexKey, Set<IssuesIndexEntry>> current) {
    this.current = ImmutableMap.copyOf(current);
  }

  public static IssuesIndexMerge from(Map<IssuesIndexKey, Set<IssuesIndexEntry>> current) {
    return new IssuesIndexMerge(current);
  }

  public IssuesIndexMerge with(Map<IssuesIndexKey, Set<IssuesIndexEntry>> update) {
    this.update = ImmutableMap.copyOf(update);
    return this;
  }

  public Map<IssuesIndexKey, Set<IssuesIndexEntry>> get() {

    final ImmutableSet<String> filesAffectedByUpdate = FluentIterable.from(update.entrySet())
        .transform(new Function<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>, String>() {
          @Override
          public String apply(Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndexKeySetEntry) {
            return issuesIndexKeySetEntry.getKey().getFullFilePath();
          }
        }).toSet();
    final ImmutableSet<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>> currentIndexEntriesWithoutIssuesFromSonarServer =
        FluentIterable.from(current.entrySet())
            .filter(new Predicate<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>>() {
              @Override
              public boolean apply(Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>> issuesIndexKeySetEntry) {
                return !filesAffectedByUpdate.contains(issuesIndexKeySetEntry.getKey().getFullFilePath());
              }
            }).toSet();
    final ImmutableSet<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>> currentIndexEntriesWithIssuesFromSonarServer =
        ImmutableSet.<Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>>>builder()
            .addAll(currentIndexEntriesWithoutIssuesFromSonarServer)
            .addAll(update.entrySet())
            .build();
    final ConcurrentMap<IssuesIndexKey, Set<IssuesIndexEntry>> newIndexMap = Maps.newConcurrentMap();
    for(Map.Entry<IssuesIndexKey, Set<IssuesIndexEntry>> entry: currentIndexEntriesWithIssuesFromSonarServer) {
      newIndexMap.put(entry.getKey(), entry.getValue());
    }
    return newIndexMap;
  }
}
