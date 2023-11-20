package io.ebean.service;

import io.ebean.Database;
import io.ebean.DatabaseBuilder;

/**
 * Creates the Database implementations. This is used internally by the EbeanServerFactory and is not currently
 * exposed as public API.
 */
public interface SpiContainer {

  /**
   * Create the EbeanServer for a given configuration.
   *
   * @param configuration The configuration information for this database.
   */
  Database createServer(DatabaseBuilder configuration);

  /**
   * Create an EbeanServer just using the name.
   * <p>
   * In this case the dataSource parameters etc will be defined on the global
   * avaje.properties file.
   * </p>
   */
  Database createServer(String name);

  /**
   * Shutdown any Ebean wide resources such as clustering.
   */
  void shutdown();
}
