package com.avaje.ebeaninternal.api;

import com.avaje.ebean.config.ServerConfig;

/**
 * Allows us to have more than one plugin (the ddl  generator) active based on flags.
 *
 * author: Richard Vowles - http://gplus.to/RichardVowles
 */
@Deprecated
public interface SpiEbeanPlugin {

  /**
   * initializes the plugin.
   *
   * @param server - the ebean server with extensions for all of the Ebean internals
   * @param serverConfig - the configured server information. It allows access to pre-collected information (but not the collector PropertySource, yet)
   */
  void setup(SpiEbeanServer server, ServerConfig serverConfig);

  /**
   * Execute the plugin (generate DDL for example).
   */
  void execute(boolean online);
}
