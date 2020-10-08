package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.ObjectGraphOrigin;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Profiling information collected.
 */
public class AutoTuneCollection {

  final List<Entry> entries = new ArrayList<>();

  public Entry add(ObjectGraphOrigin origin, OrmQueryDetail detail, String sourceQuery) {
    Entry entry = new Entry(origin, detail, sourceQuery);
    entries.add(entry);
    return entry;
  }

  public List<Entry> getEntries() {
    return entries;
  }

  /**
   * Profiling entry at a given origin point.
   */
  public static class Entry {

    /**
     * Profiling origin point.
     */
    private final ObjectGraphOrigin origin;

    /**
     * The tuned query detail.
     */
    private final OrmQueryDetail detail;

    /**
     * The original/existing query detail.
     */
    private final String originalQuery;

    /**
     * Summary execution statistics for queries related to this origin point.
     */
    private final List<EntryQuery> queries = new ArrayList<>();

    public Entry(ObjectGraphOrigin origin, OrmQueryDetail detail, String originalQuery) {
      this.origin = origin;
      this.detail = detail;
      this.originalQuery = originalQuery;
    }

    public void addQuery(EntryQuery entryQuery) {
      queries.add(entryQuery);
    }

    public ObjectGraphOrigin getOrigin() {
      return origin;
    }

    public OrmQueryDetail getDetail() {
      return detail;
    }

    public String getOriginalQuery() {
      return originalQuery;
    }

    public List<EntryQuery> getQueries() {
      return queries;
    }

  }

  /**
   * Summary query execution statistics for the origin point.
   */
  public static class EntryQuery {

    final String path;
    final long exeCount;
    final long totalBeanLoaded;
    final long totalMicros;

    public EntryQuery(String path, long exeCount, long totalBeanLoaded, long totalMicros) {
      this.path = path;
      this.exeCount = exeCount;
      this.totalBeanLoaded = totalBeanLoaded;
      this.totalMicros = totalMicros;
    }

    /**
     * Return the relative path with empty string for the origin query.
     */
    public String getPath() {
      return path;
    }

    public long getExeCount() {
      return exeCount;
    }

    public long getTotalBeanLoaded() {
      return totalBeanLoaded;
    }

    public long getTotalMicros() {
      return totalMicros;
    }
  }
}
