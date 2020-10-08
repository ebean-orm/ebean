package io.ebean.event;

import io.ebeaninternal.server.lib.ShutdownManager;

import javax.servlet.ServletContextEvent;

/**
 * Listens for webserver server starting and stopping events.
 * <p>
 * This should be used when the deployment is into a servlet container where the webapp
 * can be shutdown or redeployed without the JVM stopping.
 * </p>
 * <p>
 * If deployment is into a container where the JVM is completely shutdown (like spring boot,
 * runnable war or when using a servlet container that only contains the single webapp and
 * the JVM is shutdown then this isn't required. Instead we can just rely on the JVM shutdown
 * hook that Ebean registers.
 * </p>
 */
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
