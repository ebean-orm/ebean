package io.ebeaninternal.server.cluster;

import io.ebean.EbeanServer;

/**
 * Returns EbeanServer instances for remote message reading.
 */
public interface ServerLookup {

  /**
   * Return the EbeanServer instance by name.
   */
  EbeanServer getServer(String name);
}
