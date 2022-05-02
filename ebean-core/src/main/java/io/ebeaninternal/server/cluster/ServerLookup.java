package io.ebeaninternal.server.cluster;

import io.ebean.Database;

/**
 * Returns Database instances for remote message reading.
 */
public interface ServerLookup {

  /**
   * Return the EbeanServer instance by name.
   */
  Database getServer(String name);
}
