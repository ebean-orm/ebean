package io.ebeaninternal.server.cluster;

import io.avaje.applog.AppLog;
import io.ebean.Database;
import io.ebean.config.ContainerConfig;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Manages the cluster service.
 */
public class ClusterManager implements ServerLookup {

  private static final System.Logger clusterLogger = AppLog.getLogger("io.ebean.Cluster");

  private final ReentrantLock lock = new ReentrantLock();

  private final ConcurrentHashMap<String, Database> serverMap = new ConcurrentHashMap<>();

  private final Object monitor = new Object();

  private final ClusterBroadcast broadcast;

  private boolean started;

  private boolean shutdown;

  public ClusterManager(ContainerConfig config) {
    ClusterBroadcastFactory factory = createFactory();
    if (factory != null && config.isActive()) {
      broadcast = factory.create(this, config);
    } else {
      broadcast = null;
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
    return factory;
  }

  public void registerServer(Database server) {
    lock.lock();
    try {
      serverMap.put(server.name(), server);
      if (!started) {
        startup();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Database getServer(String name) {
    lock.lock();
    try {
      return serverMap.get(name);
    } finally {
      lock.unlock();
    }
  }

  private void startup() {
    started = true;
    if (broadcast != null) {
      broadcast.startup();
    }
  }

  /**
   * Broadcast a cache clear all event to the cluster.
   */
  public void cacheClearAll(String serverName) {
    if (broadcast != null) {
      broadcast.broadcast(new RemoteTransactionEvent(serverName).cacheClearAll());
    }
  }

  /**
   * Broadcast a cache clear event to the cluster.
   */
  public void cacheClear(String serverName, Class<?> beanType) {
    if (broadcast != null) {
      broadcast.broadcast(new RemoteTransactionEvent(serverName).cacheClear(beanType));
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
      if (clusterLogger.isLoggable(DEBUG)) {
        clusterLogger.log(DEBUG, "sending: {0}", event);
      }
      broadcast.broadcast(event);
    }
  }

  /**
   * Shutdown the service and Deregister from the cluster.
   */
  public void shutdown() {
    if (broadcast != null && !shutdown) {
      shutdown = true;
      broadcast.shutdown();
    }
  }
}
