package com.avaje.ebeaninternal.server.cluster;

import java.util.concurrent.ConcurrentHashMap;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
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

  public ClusterManager() {

    String clusterType = GlobalProperties.get("ebean.cluster.type", null);
    if (clusterType == null || clusterType.trim().length() == 0) {
      // not clustering this instance
      this.broadcast = null;

    } else {

      try {
        if ("mcast".equalsIgnoreCase(clusterType)) {
          this.broadcast = new McastClusterManager();

        } else if ("socket".equalsIgnoreCase(clusterType)) {
          this.broadcast = new SocketClusterBroadcast();

        } else {
          logger.info("Clustering using [" + clusterType + "]");
          this.broadcast = (ClusterBroadcast) ClassUtil.newInstance(clusterType);
        }

      } catch (Exception e) {
        String msg = "Error initialising ClusterManager type [" + clusterType + "]";
        logger.error(msg, e);
        throw new RuntimeException(e);
      }
    }
  }

  public void registerServer(EbeanServer server) {
    synchronized (monitor) {
      if (!started) {
        startup();
      }
      serverMap.put(server.getName(), server);
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
