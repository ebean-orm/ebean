package com.avaje.ebean.plugin;

/**
 * A 'plugin' that wants to be configured on startup so it can use features of the EbeanServer itself.
 */
public interface SpiServerPlugin {

  /**
   * Configure the plugin.
   */
  void configure(SpiServer server);

  /**
   * Called just before the server starts indicating if it is coming up in online mode.
   */
  void online(boolean online);

  /**
   * Called when the server is shutting down.
   * <p>
   * Plugins should shutdown any resources they are using cleanly.
   * </p>
   */
  void shutdown();
}
