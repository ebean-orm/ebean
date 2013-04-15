package com.avaje.ebean.common;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.ServerConfig;

/**
 * Creates the EbeanServer implementations. This is used by the Ebean singleton
 * to determine the implementation for each server name.
 * <p>
 * Note that on a remote client it is expected that this factory will return
 * EbeanServers that behave as a proxy using http or tcp sockets etc to talk to
 * the EbeanServer on the application server.
 * </p>
 */
public interface BootupEbeanManager {

  /**
   * Create the EbeanServer for a given configuration.
   * 
   * @param configuration
   *          The configuration information for this server.
   */
  public EbeanServer createServer(ServerConfig configuration);

  /**
   * Create an EbeanServer just using the name.
   * <p>
   * In this case the dataSource parameters etc will be defined on the global
   * avaje.properties file.
   * </p>
   */
  public EbeanServer createServer(String name);

  /**
   * Shutdown any Ebean wide resources such as clustering.
   */
  public void shutdown();
}
