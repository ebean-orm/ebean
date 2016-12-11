package io.ebeaninternal.server.cluster;

import io.ebean.EbeanServer;
import io.ebean.config.ContainerConfig;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the cluster service.
 */
public class ClusterManager {

  private static final Logger clusterLogger = LoggerFactory.getLogger("org.avaje.ebean.Cluster");

  private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

  private final ConcurrentHashMap<String, EbeanServer> serverMap = new ConcurrentHashMap<>();

  private final Object monitor = new Object();

  private final ClusterBroadcast broadcast;

  private boolean started;

  public ClusterManager(ContainerConfig config) {
    if (!config.isClusterActive()) {
      broadcast = null;
    } else {
      ClusterBroadcastFactory factory = createFactory();
      broadcast = factory.create(this, config.getProperties());
    }
  }

  /**
   * Return the ClusterTransportFactory via ServiceLoader.
   */
  private ClusterBroadcastFactory createFactory() {

    ServiceLoader<ClusterBroadcastFactory> load = ServiceLoader.load(ClusterBroadcastFactory.class);
    ClusterBroadcastFactory factory = null;
    Iterator<ClusterBroadcastFactory> iterator = load.iterator();
    if (iterator.hasNext()) {
      factory = iterator.next();
    }
    if (factory == null) {
      throw new IllegalStateException("No ClusterTransportFactory found in classpath. "
        + " Probably need to add the avaje-ebeanorm-cluster dependency");
    }
    return factory;
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
      broadcast.startup();
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
  public void broadcast(RemoteTransactionEvent event) {
    if (broadcast != null) {
      if (clusterLogger.isDebugEnabled()) {
        clusterLogger.debug("sending: {}", event);
      }
      broadcast.broadcast(event);
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
