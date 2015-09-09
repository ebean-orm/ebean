package com.avaje.ebeaninternal.server.autofetch;

import com.avaje.ebean.bean.NodeUsageListener;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Profiling listener gets call backs for node usage and the associated query executions.
 */
public interface ProfilingListener extends NodeUsageListener {

  /**
   * Collect summary statistics for a query executed for the given node.
   *
   * @param node   the node relative to the origin point
   * @param beans  the number of beans loaded by the query
   * @param micros the query execution in microseconds
   */
  void collectQueryInfo(ObjectGraphNode node, long beans, long micros);

  /**
   * Return true if this request should be profiled (based on the
   * profiling ratio and collection count for this origin).
   */
  boolean isProfileRequest(ObjectGraphNode origin, SpiQuery<?> query);
}
