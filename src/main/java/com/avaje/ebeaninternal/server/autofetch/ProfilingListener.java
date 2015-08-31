package com.avaje.ebeaninternal.server.autofetch;

import com.avaje.ebean.bean.NodeUsageListener;
import com.avaje.ebean.bean.ObjectGraphNode;

/**
 * Profiling listener gets call backs for node usage and the associated query executions.
 */
public interface ProfilingListener extends NodeUsageListener {

  void collectQueryInfo(ObjectGraphNode node, long beans, long micros);

}
