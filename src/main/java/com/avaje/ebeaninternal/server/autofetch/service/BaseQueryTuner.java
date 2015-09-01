package com.avaje.ebeaninternal.server.autofetch.service;

import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.config.AutofetchConfig;
import com.avaje.ebean.config.AutofetchMode;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.autofetch.ProfilingListener;

import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class BaseQueryTuner {

  private final boolean queryTuning;

  private boolean profiling;

  private final AutofetchMode mode;

  /**
   * Map of the tuned query details per profile query point.
   */
  private final Map<String, TunedQueryInfo> tunedQueryInfoMap = new ConcurrentHashMap<String, TunedQueryInfo>();


  private final SpiEbeanServer server;

  private final ProfilingListener profilingListener;

  public BaseQueryTuner(AutofetchConfig config, SpiEbeanServer server, ProfilingListener profilingListener) {
    this.server = server;
    this.profilingListener = profilingListener;
    this.mode = config.getMode();
    this.queryTuning = config.isQueryTuning();
    this.profiling = config.isProfiling();
  }

  /**
   * Load the tuned query information.
   */
  public void load(String key, TunedQueryInfo queryInfo) {
    tunedQueryInfoMap.put(key, queryInfo);
  }

  /**
   * Auto tune the query and enable profiling.
   */
  public boolean tuneQuery(SpiQuery<?> query) {

    if (!queryTuning && !profiling) {
      return false;
    }

    if (!useAutoTune(query)) {
      // not using autoFetch for this query
      return false;
    }

    if (query.getParentNode() != null) {
      // This is a +lazy/+query query with profiling on.
      // We continue to collect the profiling information.
      query.setProfilingListener(profilingListener);
      return true;
    }

    // create a query point to identify the query
    CallStack stack = server.createCallStack();
    ObjectGraphNode origin = query.setOrigin(stack);

    if (profiling) {
      if (profilingListener.isProfileRequest(origin)) {
        // collect more profiling based on profiling rate etc
        query.setProfilingListener(profilingListener);
      }
    }

    if (queryTuning) {
      // get current "tuned fetch" for this query point
      TunedQueryInfo tuneInfo = tunedQueryInfoMap.get(origin.getOriginQueryPoint().getKey());
      return tuneInfo != null && tuneInfo.tuneQuery(query);
    }
    return false;
  }

  /**
   * Return true if we should try to tune this query.
   */
  private boolean useAutoTune(SpiQuery<?> query) {

    if (query.isLoadBeanCache()) {
      // when loading the cache don't tune the query
      // as we want full objects loaded into the cache
      return false;
    }

    Boolean autoFetch = query.isAutofetch();
    if (autoFetch != null) {
      // explicitly set...
      return autoFetch;

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
          throw new PersistenceException("Invalid autoFetchMode " + mode);
      }
    }
  }
}
