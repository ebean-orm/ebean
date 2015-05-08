package com.avaje.ebeaninternal.server.cluster.socket;

import com.avaje.ebean.config.ContainerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.cluster.ClusterBroadcast;
import com.avaje.ebeaninternal.server.cluster.ClusterManager;
import com.avaje.ebeaninternal.server.cluster.DataHolder;
import com.avaje.ebeaninternal.server.cluster.SerialiseTransactionHelper;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Broadcast messages across the cluster using sockets.
 */
public class SocketClusterBroadcast implements ClusterBroadcast {

  private static final Logger logger = LoggerFactory.getLogger(SocketClusterBroadcast.class);

  private final SocketClient local;

  private final HashMap<String, SocketClient> clientMap;

  private final SocketClusterListener listener;

  private SocketClient[] members;

  private ClusterManager clusterManager;

  private final TxnSerialiseHelper txnSerialiseHelper = new TxnSerialiseHelper();

  private final AtomicInteger txnOutgoing = new AtomicInteger();
  private final AtomicInteger txnIncoming = new AtomicInteger();

  public SocketClusterBroadcast(ContainerConfig containerConfig) {

    ContainerConfig.SocketConfig socketConfig = containerConfig.getSocketConfig();

    String localHostPort = socketConfig.getLocalHostPort();
    List<String> members = socketConfig.getMembers();

    logger.info("Clustering using Sockets local[" + localHostPort + "] members[" + members + "]");

    this.local = new SocketClient(parseFullName(localHostPort));
    this.clientMap = new HashMap<String, SocketClient>();

    for (String memberHostPort : members) {
      InetSocketAddress member = parseFullName(memberHostPort);
      SocketClient client = new SocketClient(member);
      if (!local.getHostPort().equalsIgnoreCase(client.getHostPort())) {
        // don't add the local one ...
        clientMap.put(client.getHostPort(), client);
      }
    }

    this.members = clientMap.values().toArray(new SocketClient[clientMap.size()]);
    this.listener = new SocketClusterListener(this, local.getPort(), socketConfig.getCoreThreads(), socketConfig.getMaxThreads(), socketConfig.getThreadPoolName());
  }

  public String getHostPort() {
    return local.getHostPort();
  }

  /**
   * Return the current status of this instance.
   */
  public SocketClusterStatus getStatus() {

    // count of online members
    int currentGroupSize = 0;
    for (int i = 0; i < members.length; i++) {
      if (members[i].isOnline()) {
        ++currentGroupSize;
      }
    }
    int txnIn = txnIncoming.get();
    int txnOut = txnOutgoing.get();

    return new SocketClusterStatus(currentGroupSize, txnIn, txnOut);
  }

  public void startup(ClusterManager clusterManager) {

    this.clusterManager = clusterManager;
    try {
      listener.startListening();
      register();

    } catch (IOException e) {
      throw new PersistenceException(e);
    }
  }

  public void shutdown() {
    deregister();
    listener.shutdown();
  }

  /**
   * Register with all the other members of the Cluster.
   */
  private void register() {

    SocketClusterMessage h = SocketClusterMessage.register(local.getHostPort(), true);

    for (int i = 0; i < members.length; i++) {
      boolean online = members[i].register(h);
      logger.info("Cluster Member [{}] online[{}]", members[i].getHostPort(), online);
    }
  }

  protected void setMemberOnline(String fullName, boolean online) throws IOException {
    synchronized (clientMap) {
      logger.info("Cluster Member [{}] online[{}]", fullName, online);
      SocketClient member = clientMap.get(fullName);
      member.setOnline(online);
    }
  }

  private void send(SocketClient client, SocketClusterMessage msg) {

    try {
      // alternative would be to connect/disconnect here but prefer to use keepalive
      if (logger.isTraceEnabled()) {
        logger.trace("... send to member {} broadcast msg: {}", client, msg);
      }
      client.send(msg);

    } catch (Exception ex) {
      logger.error("Error sending message", ex);
      try {
        client.reconnect();
      } catch (IOException e) {
        logger.error("Error trying to reconnect", ex);
      }
    }
  }

  /**
   * Send the payload to all the members of the cluster.
   */
  public void broadcast(RemoteTransactionEvent remoteTransEvent) {
    try {
      txnOutgoing.incrementAndGet();
      DataHolder dataHolder = txnSerialiseHelper.createDataHolder(remoteTransEvent);
      SocketClusterMessage msg = SocketClusterMessage.transEvent(dataHolder);
      broadcast(msg);
    } catch (Exception e) {
      logger.error("Error sending RemoteTransactionEvent " + remoteTransEvent + " to cluster members.", e);
    }
  }

  protected void broadcast(SocketClusterMessage msg) {

    if (logger.isTraceEnabled()) {
      logger.trace("... broadcast msg: "+msg);
    }
    for (int i = 0; i < members.length; i++) {
      send(members[i], msg);
    }
  }

  /**
   * Leave the cluster.
   */
  private void deregister() {

    SocketClusterMessage h = SocketClusterMessage.register(local.getHostPort(), false);
    broadcast(h);
    for (int i = 0; i < members.length; i++) {
      members[i].disconnect();
    }
  }

  /**
   * Process an incoming Cluster message.
   */
  protected boolean process(SocketConnection request) throws IOException, ClassNotFoundException {

    try {
      SocketClusterMessage h = (SocketClusterMessage) request.readObject();
      if (logger.isTraceEnabled()) {
        logger.trace("... received msg: {}", h);
      }

      if (h.isRegisterEvent()) {
        setMemberOnline(h.getRegisterHost(), h.isRegister());

      } else {
        txnIncoming.incrementAndGet();
        DataHolder dataHolder = h.getDataHolder();
        RemoteTransactionEvent transEvent = txnSerialiseHelper.read(dataHolder);
        transEvent.run();
      }

      // instance shutting down
      return h.isRegisterEvent() && !h.isRegister();

    } catch (InterruptedIOException e) {
      logger.info("Timeout waiting for message", e);
      try {
        request.disconnect();
      } catch (IOException ex) {
        logger.info("Error disconnecting after timeout", ex);
      }
      return true;

    } catch (EOFException e) {
      logger.info("EOF disconnecting");
      return true;
    } catch (IOException e) {
      logger.info("IO Error waiting/reading message", e);
      return true;
    }
  }

  /**
   * Parse a host:port into a InetSocketAddress.
   */
  private InetSocketAddress parseFullName(String hostAndPort) {

    try {
      hostAndPort = hostAndPort.trim();
      int colonPos = hostAndPort.indexOf(":");
      if (colonPos == -1) {
        String msg = "No colon \":\" in " + hostAndPort;
        throw new IllegalArgumentException(msg);
      }
      String host = hostAndPort.substring(0, colonPos);
      String sPort = hostAndPort.substring(colonPos + 1, hostAndPort.length());
      int port = Integer.parseInt(sPort);

      return new InetSocketAddress(host, port);

    } catch (Exception ex) {
      throw new RuntimeException("Error parsing [" + hostAndPort + "] for the form [host:port]", ex);
    }
  }

  class TxnSerialiseHelper extends SerialiseTransactionHelper {

    @Override
    public SpiEbeanServer getEbeanServer(String serverName) {
      return (SpiEbeanServer) clusterManager.getServer(serverName);
    }
  }
}
