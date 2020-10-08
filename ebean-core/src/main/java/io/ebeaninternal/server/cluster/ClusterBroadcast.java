package io.ebeaninternal.server.cluster;

import io.ebeaninternal.server.transaction.RemoteTransactionEvent;


/**
 * Sends messages to the cluster members.
 */
public interface ClusterBroadcast {

  /**
   * Inform the other cluster members that this instance has come online and
   * start any listeners etc.
   */
  void startup();

  /**
   * Inform the other cluster members that this instance is leaving and
   * shutdown any listeners.
   */
  void shutdown();

  /**
   * Send a transaction event to all the members of the cluster.
   */
  void broadcast(RemoteTransactionEvent remoteTransEvent);
}
