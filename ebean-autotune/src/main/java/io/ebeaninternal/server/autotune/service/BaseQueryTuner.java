package io.ebeaninternal.server.autotune.service;

import io.ebean.bean.CallOrigin;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.AutoTuneConfig;
import io.ebean.config.AutoTuneMode;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.autotune.ProfilingListener;
import io.ebeaninternal.server.autotune.model.Origin;
import io.ebeaninternal.server.querydefn.OrmQueryDetail;

import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class BaseQueryTuner {

  private final boolean queryTuning;

  private final boolean profiling;

  private final AutoTuneMode mode;

  /**
   * Map of the tuned query details per profile query point.
   */
  private final Map<String, TunedQueryInfo> tunedQueryInfoMap = new ConcurrentHashMap<>();

  private final SpiEbeanServer server;

  private final ProfilingListener profilingListener;

  /**
   * Flag set true when there is no profiling or query tuning.
   */
  private final boolean skipAll;

  BaseQueryTuner(AutoTuneConfig config, SpiEbeanServer server, ProfilingListener profilingListener) {
    this.server = server;
    this.profilingListener = profilingListener;
    this.mode = config.getMode();
    this.queryTuning = config.isQueryTuning();
    this.profiling = config.isProfiling();
    this.skipAll = !queryTuning && !profiling;
  }

  /**
   * Return all the current tuned query entries.
   */
  public Collection<TunedQueryInfo> getAll() {
    return tunedQueryInfoMap.values();
  }

  /**
   * Put a query tuning entry.
   */
  public void put(Origin origin) {

    tunedQueryInfoMap.put(origin.getKey(), new TunedQueryInfo(origin));
  }

  /**
   * Load the tuned query information.
   */
  public void load(String key, TunedQueryInfo queryInfo) {
    tunedQueryInfoMap.put(key, queryInfo);
  }

  /**
   * Return the detail currently used for tuning.
   * This returns null if there is currently no matching tuning.
   */
  public OrmQueryDetail get(String key) {
    TunedQueryInfo info = tunedQueryInfoMap.get(key);
    return (info == null) ? null : info.getTunedDetail();
  }

  /**
   * Auto tune the query and enable profiling.
   */
  boolean tuneQuery(SpiQuery<?> query) {

    if (skipAll || !tunableQuery(query)) {
      return false;
    }

    if (query.getProfilingListener() != null) {
      // profiling secondary query
      return false;
    }

    if (!useTuning(query)) {
      if (profiling) {
        profiling(query, server.createCallOrigin());
      }
      return false;
    }

    if (query.getParentNode() != null) {
      // This is a +lazy/+query query with profiling on.
      // We continue to collect the profiling information.
      query.setProfilingListener(profilingListener);
      return true;
    }

    // create a query point to identify the query
    CallOrigin callOrigin = server.createCallOrigin();
    ObjectGraphNode origin = query.setOrigin(callOrigin);

    if (profiling) {
      if (profilingListener.isProfileRequest(origin, query)) {
        // collect more profiling based on profiling rate etc
        query.setProfilingListener(profilingListener);
      }
    }

    if (queryTuning) {
      // get current "tuned fetch" for this query point
      TunedQueryInfo tuneInfo = tunedQueryInfoMap.get(origin.origin().key());
      return tuneInfo != null && tuneInfo.tuneQuery(query);
    }
    return false;
  }

  /**
   * Return false for row count, find ids, subQuery, delete and Versions queries.
   * <p>
   * These queries are not applicable for autoTune in that they don't have a select/fetch (fetch group).
   * </p>
   * <p>
   * We also exclude queries that are explicitly set to load the L2 bean cache as we want full beans
   * in that case.
   * </p>
   */
  private boolean tunableQuery(SpiQuery<?> query) {
    SpiQuery.Type type = query.getType();
    switch (type) {
      case COUNT:
      case ATTRIBUTE:
      case ATTRIBUTE_SET:
      case ID_LIST:
      case UPDATE:
      case DELETE:
      case SQ_EXISTS:
      case SQ_EX:
        return false;
      default:
        // not using autoTune when explicitly loading the l2 bean cache
        // or when using Versions query
        return !query.isForceHitDatabase() && SpiQuery.TemporalMode.VERSIONS != query.getTemporalMode();
    }
  }

  private void profiling(SpiQuery<?> query, CallOrigin call) {

    // create a query point to identify the query
    ObjectGraphNode origin = query.setOrigin(call);
    if (profilingListener.isProfileRequest(origin, query)) {
      // collect more profiling based on profiling rate etc
      query.setProfilingListener(profilingListener);
    }
  }

  /**
   * Return true if we should try to tune this query.
   */
  private boolean useTuning(SpiQuery<?> query) {

    Boolean autoTune = query.isAutoTune();
    if (autoTune != null) {
      // explicitly set...
      return autoTune;

    } else {
      // determine using implicit mode...
      switch (mode) {
        case DEFAULT_ON:
          return true;

        case DEFAULT_OFF:
          return false;

        case DEFAULT_ONIFEMPTY:
          return query.isDetailEmpty();

        default:
          throw new PersistenceException("Invalid AutoTuneMode " + mode);
      }
    }
  }

  /**
   * Return the keys as a set.
   */
  public Set<String> keySet() {
    return tunedQueryInfoMap.keySet();
  }
}
