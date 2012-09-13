/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.cluster;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.cluster.mcast.McastClusterManager;
import com.avaje.ebeaninternal.server.cluster.socket.SocketClusterBroadcast;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

/**
 * Manages the cluster service.
 */
public class ClusterManager {

  private static final Logger logger = Logger.getLogger(ClusterManager.class.getName());

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
        logger.log(Level.SEVERE, msg, e);
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
