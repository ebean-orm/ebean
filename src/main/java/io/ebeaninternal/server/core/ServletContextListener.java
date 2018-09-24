package io.ebeaninternal.server.core;

import io.ebeaninternal.server.lib.ShutdownManager;

import javax.servlet.ServletContextEvent;

/**
 * Deprecated - migrate to io.ebean.event.ServletContextListener.
 * <p>
 * Listens for webserver server starting and stopping events.
 * <p>
 * Register this listener in the web.xml configuration file. This will listen
 * for startup and shutdown events.
 * </p>
 */
@Deprecated
public class ServletContextListener implements javax.servlet.ServletContextListener {

  /**
   * The servlet container is stopping.
   */
  @Override
  public void contextDestroyed(ServletContextEvent event) {
    ShutdownManager.shutdown();
  }

  /**
   * Do nothing on startup.
   */
  @Override
  public void contextInitialized(ServletContextEvent event) {

  }

}
