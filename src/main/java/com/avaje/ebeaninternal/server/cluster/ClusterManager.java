package com.avaje.ebeaninternal.server.cluster;

import java.util.concurrent.ConcurrentHashMap;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebeaninternal.server.cluster.mcast.McastClusterManager;
import com.avaje.ebeaninternal.server.cluster.socket.SocketClusterBroadcast;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the cluster service.
 */
public class ClusterManager {

  private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

  private final ConcurrentHashMap<String, EbeanServer> serverMap = new ConcurrentHashMap<String, EbeanServer>();

  private final Object monitor = new Object();

  private final ClusterBroadcast broadcast;

  private boolean started;

  public ClusterManager(ContainerConfig containerConfig) {

    ContainerConfig.ClusterMode mode = containerConfig.getMode();
    try {
      switch (mode) {
        case SOCKET: {
          this.broadcast = new SocketClusterBroadcast(containerConfig);
          break;
        }
        case MULTICAST: {
          this.broadcast = new McastClusterManager(containerConfig);
          break;
        }
        default: {
          this.broadcast = null;
        }
      }

    } catch (Exception e) {
      logger.error("Error initialising ClusterManager type [" + mode + "]", e);
      throw new RuntimeException(e);
    }
  }

  public void registerServer(EbeanServer server) {
    synchronized (monitor) {
      serverMap.put(server.getName(), server);
      if (!started) {
        startup();
      }
    }
  }

  public EbeanServer getServer(String name) {
    synchronized (monitor) {
      return serverMap.get(name);
    }
  }

  private void startup() {
    started = true;
    if (broadcast != null) {
      broadcast.startup(this);
    }
  }

  /**
   * Return true if clustering is on.
   */
  public boolean isClustering() {
    return broadcast != null;
  }

  /**
   * Send the message headers and payload to every server in the cluster.
   */
  public void broadcast(RemoteTransactionEvent remoteTransEvent) {
    if (broadcast != null) {
      broadcast.broadcast(remoteTransEvent);
    }
  }

  /**
   * Shutdown the service and Deregister from the cluster.
   */
  public void shutdown() {
    if (broadcast != null) {
      logger.info("ClusterManager shutdown ");
      broadcast.shutdown();
    }
  }
}
