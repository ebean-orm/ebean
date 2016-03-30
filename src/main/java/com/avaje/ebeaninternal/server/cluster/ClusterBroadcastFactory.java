package com.avaje.ebeaninternal.server.cluster;

import java.util.Properties;

/**
 * Factory to create the cluster broadcast service.
 */
public interface ClusterBroadcastFactory {

  /**
   * Create the cluster transport with the manager and deployment properties.
   */
  ClusterBroadcast create(ClusterManager manager, Properties properties);
}
