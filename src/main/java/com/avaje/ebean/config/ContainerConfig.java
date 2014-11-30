package com.avaje.ebean.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Configuration for the container that holds the EbeanServer instances.
 * <p>
 * Provides configuration for cluster communication (if clustering is used). The cluster communication is
 * used to invalidate appropriate parts of the L2 cache across the cluster.
 */
public class ContainerConfig {

  /**
   * Communication mode used for clustering.
   */
  public enum ClusterMode {

    /**
     * No clustering.
     */
    NONE,

    /**
     * Use Multicast networking for cluster wide communication.
     */
    MULTICAST,

    /**
     * Use TCP Sockets for cluster wide communication.
     */
    SOCKET
  }

  /**
   * The cluster mode to use.
   */
  ClusterMode mode = ClusterMode.NONE;

  /**
   * Configuration if using TCP sockets for clustering communication.
   */
  SocketConfig socketConfig = new SocketConfig();

  /**
   * Configuration if using Multicast for clustering communication.
   */
  MulticastConfig multicastConfig = new MulticastConfig();


  // -------------------------------------------------------------------------------------------
  // MulticastConfig

  /**
   * The configuration for clustering using Multicast networking.
   */
  public static class MulticastConfig {

    int managerSleepMillis = 80;
    int lastSendTimeFreqSecs = 300;//5mins
    int lastStatusTimeFreqSecs = 600;//10mins
    int maxResendOutgoingAttempts = 200;
    int maxResendIncomingRequests = 50;

    int listenPort;
    String listenAddress;
    int sendPort;
    String sendAddress;

    // Note 1500 is Ethernet MTU and this must be less than UDP max packet size of 65507
    int maxSendPacketSize = 1500;

    // Whether to send packets even when there are no other members online
    boolean sendWithNoMembers = true;

    // When multiple instances are on same box you need to broadcast back locally
    boolean disableLoopback;
    int listenTimeToLive = -1;
    int listenTimeout = 1000;
    int listenBufferSize = 65500;
    // For multihomed environment the address the listener should bind to
    String listenBindAddress;

    /**
     * Return the manager sleep millis.
     */
    public int getManagerSleepMillis() {
      return managerSleepMillis;
    }

    /**
     * Set the manager sleep millis.
     */
    public void setManagerSleepMillis(int managerSleepMillis) {
      this.managerSleepMillis = managerSleepMillis;
    }

    /**
     * Return the last send time frequency.
     */
    public int getLastSendTimeFreqSecs() {
      return lastSendTimeFreqSecs;
    }

    /**
     * Set the last send time frequency.
     */
    public void setLastSendTimeFreqSecs(int lastSendTimeFreqSecs) {
      this.lastSendTimeFreqSecs = lastSendTimeFreqSecs;
    }

    /**
     * Return the last status time frequency.
     */
    public int getLastStatusTimeFreqSecs() {
      return lastStatusTimeFreqSecs;
    }

    /**
     * Set the last status time frequency.
     */
    public void setLastStatusTimeFreqSecs(int lastStatusTimeFreqSecs) {
      this.lastStatusTimeFreqSecs = lastStatusTimeFreqSecs;
    }

    /**
     * Return the maximum number of times we will try to re-send a given packet before giving up sending
     */
    public int getMaxResendOutgoingAttempts() {
      return maxResendOutgoingAttempts;
    }

    /**
     * Set the maximum retry attempts for outgoing messages.
     */
    public void setMaxResendOutgoingAttempts(int maxResendOutgoingAttempts) {
      this.maxResendOutgoingAttempts = maxResendOutgoingAttempts;
    }

    /**
     * Return the maximum number of times we will ask for a packet to be resent to us before giving up asking.
     */
    public int getMaxResendIncomingRequests() {
      return maxResendIncomingRequests;
    }

    /**
     * Set the maximum retry attempts for incoming messages.
     */
    public void setMaxResendIncomingRequests(int maxResendIncomingRequests) {
      this.maxResendIncomingRequests = maxResendIncomingRequests;
    }

    /**
     * Return the listen port.
     */
    public int getListenPort() {
      return listenPort;
    }

    /**
     * Set the listen port.
     */
    public void setListenPort(int port) {
      this.listenPort = port;
    }

    /**
     * Return the listen address.
     */
    public String getListenAddress() {
      return listenAddress;
    }

    /**
     * Set the listen address.
     */
    public void setListenAddress(String listenAddress) {
      this.listenAddress = listenAddress;
    }

    /**
     * Return the send port.
     */
    public int getSendPort() {
      return sendPort;
    }

    /**
     * Set the send port.
     */
    public void setSendPort(int sendPort) {
      this.sendPort = sendPort;
    }

    /**
     * Return the send address.
     */
    public String getSendAddress() {
      return sendAddress;
    }

    /**
     * Set the send address.
     */
    public void setSendAddress(String sendAddress) {
      this.sendAddress = sendAddress;
    }

    /**
     * Return the maximum send packet size.
     */
    public int getMaxSendPacketSize() {
      return maxSendPacketSize;
    }

    /**
     * Set the maximum send packet size.  Note 1500 is Ethernet MTU and this must be less than UDP max packet size of 65507.
     */
    public void setMaxSendPacketSize(int maxSendPacketSize) {
      this.maxSendPacketSize = maxSendPacketSize;
    }

    /**
     * Return true if send messages when no other members in the cluster are up.
     */
    public boolean isSendWithNoMembers() {
      return sendWithNoMembers;
    }

    /**
     * Set true if send messages when no other members in the cluster are up.
     */
    public void setSendWithNoMembers(boolean sendWithNoMembers) {
      this.sendWithNoMembers = sendWithNoMembers;
    }

    /**
     * Return true if loopback is disabled. When multiple instances are on same box you need to broadcast back locally.
     */
    public boolean isDisableLoopback() {
      return disableLoopback;
    }

    /**
     * Set if loopback is disabled. When multiple instances are on same box you need to broadcast back locally.
     */
    public void setDisableLoopback(boolean disableLoopback) {
      this.disableLoopback = disableLoopback;
    }

    /**
     * Return the listen time to live.
     */
    public int getListenTimeToLive() {
      return listenTimeToLive;
    }

    /**
     * Set the listen time to live.
     */
    public void setListenTimeToLive(int listenTimeToLive) {
      this.listenTimeToLive = listenTimeToLive;
    }

    /**
     * Return the listen timeout.
     */
    public int getListenTimeout() {
      return listenTimeout;
    }

    /**
     * set the listen timeout.
     */
    public void setListenTimeout(int listenTimeout) {
      this.listenTimeout = listenTimeout;
    }

    /**
     * Return the listen buffer size.
     */
    public int getListenBufferSize() {
      return listenBufferSize;
    }

    /**
     * Set the listen buffer size.
     */
    public void setListenBufferSize(int listenBufferSize) {
      this.listenBufferSize = listenBufferSize;
    }

    /**
     * Return the listener bind address (optional). For multihomed environment the address the listener should bind to.
     */
    public String getListenBindAddress() {
      return listenBindAddress;
    }

    /**
     * Set the listener bind address (optional). For multihomed environment the address the listener should bind to.
     */
    public void setListenBindAddress(String listenBindAddress) {
      this.listenBindAddress = listenBindAddress;
    }
  }

  // -------------------------------------------------------------------------------------------
  // SocketConfig

  /**
   * Configuration for clustering using TCP sockets.
   * <p>
   * This is good for when there are relatively small number of cluster members.
   */
  public static class SocketConfig {

    /**
     * This local server in host:port format.
     */
    String localHostPort;

    /**
     * All the cluster members in host:port format.
     */
    List<String> members = new ArrayList<String>();

    /**
     * core threads for the associated thread pool.
     */
    int coreThreads = 2;

    /**
     * Max threads for the associated thread pool.
     */
    int maxThreads = 16;

    String threadPoolName = "EbeanCluster";

    /**
     * Return the host and port for this server instance.
     */
    public String getLocalHostPort() {
      return localHostPort;
    }

    /**
     * Set the host and port for this server instance.
     */
    public void setLocalHostPort(String localHostPort) {
      this.localHostPort = localHostPort;
    }

    /**
     * Return all the host and port for all the members of the cluster.
     */
    public List<String> getMembers() {
      return members;
    }

    /**
     * Set all the host and port for all the members of the cluster.
     */
    public void setMembers(List<String> members) {
      this.members = members;
    }

    /**
     * Return the number of core threads to use.
     */
    public int getCoreThreads() {
      return coreThreads;
    }

    /**
     * Set the number of core threads to use.
     */
    public void setCoreThreads(int coreThreads) {
      this.coreThreads = coreThreads;
    }

    /**
     * Return the number of max threads to use.
     */
    public int getMaxThreads() {
      return maxThreads;
    }

    /**
     * Set the number of max threads to use.
     */
    public void setMaxThreads(int maxThreads) {
      this.maxThreads = maxThreads;
    }

    /**
     * Return the thread pool name.
     */
    public String getThreadPoolName() {
      return threadPoolName;
    }

    /**
     * Set the thread pool name.
     */
    public void setThreadPoolName(String threadPoolName) {
      this.threadPoolName = threadPoolName;
    }
  }

  // -------------------------------------------------------------------------------------------
  // Members

  /**
   * Load the settings from properties.
   */
  public void loadFromProperties(Properties properties) {
    //TODO
  }

  /**
   * Return the cluster mode.
   */
  public ClusterMode getMode() {
    return mode;
  }

  /**
   * Set the cluster mode.
   */
  public void setMode(ClusterMode mode) {
    this.mode = mode;
  }

  /**
   * Return the socket communication configuration.
   */
  public SocketConfig getSocketConfig() {
    return socketConfig;
  }

  /**
   * Set the socket communication configuration.
   */
  public void setSocketConfig(SocketConfig socketConfig) {
    this.socketConfig = socketConfig;
  }

  /**
   * Return the multicast communication configuration.
   */
  public MulticastConfig getMulticastConfig() {
    return multicastConfig;
  }

  /**
   * Set the multicast communication configuration.
   */
  public void setMulticastConfig(MulticastConfig multicastConfig) {
    this.multicastConfig = multicastConfig;
  }
}
