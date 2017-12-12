package io.ebeaninternal.server.cluster;

import io.ebean.config.ContainerConfig;

/**
 * Factory to create the cluster broadcast service.
 */
public interface ClusterBroadcastFactory {

  /**
   * Create the cluster transport with the manager and deployment properties.
   */
  ClusterBroadcast create(ClusterManager manager, ContainerConfig config);
}
