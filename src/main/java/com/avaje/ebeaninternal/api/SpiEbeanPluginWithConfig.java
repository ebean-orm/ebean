package com.avaje.ebeaninternal.api;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;

/**
 * Extends SpiEbeanPlugin with an early call to setup configuration.
 */
public interface SpiEbeanPluginWithConfig extends SpiEbeanPlugin {

  /**
   * Modify the ServerConfig.
   *
   * This occurs early prior to the EbeanServer being built.
   */
  void modifyServerConfig(ServerConfig serverConfig);

}
