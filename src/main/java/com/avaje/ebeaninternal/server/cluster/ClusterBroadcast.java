package com.avaje.ebeaninternal.server.cluster;

import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;


/**
 * Sends messages to the cluster members.
 */
public interface ClusterBroadcast {

    /**
     * Inform the other cluster members that this instance has come online and
     * start any listeners etc.
     */
    public void startup(ClusterManager clusterManager);

    /**
     * Inform the other cluster members that this instance is leaving and
     * shutdown any listeners.
     */
    public void shutdown();
    
    /**
     * Send a transaction event to all the members of the cluster.
     */
    public void broadcast(RemoteTransactionEvent remoteTransEvent);

}
