package com.avaje.ebeaninternal.api;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;

/**
 * Allows us to have more than one plugin (the ddl  generator) active based on flags.
 *
 * author: Richard Vowles - http://gplus.to/RichardVowles
 */
public interface SpiEbeanPlugin {
  /**
   * initializes the plugin.
   *
   * @param server - the ebean server with extensions for all of the Ebean internals
   * @param dbPlatform - the database we are using - this is available from the server, but it is provided for convenience
   * @param serverConfig - the configured server information. It allows access to pre-collected information (but not the collector PropertySource, yet)
   */
  public void setup(SpiEbeanServer server, DatabasePlatform dbPlatform, ServerConfig serverConfig);

  public void execute(boolean online);
}
