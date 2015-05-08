package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.server.lib.ShutdownManager;

import javax.servlet.ServletContextEvent;

/**
 * Listens for webserver server starting and stopping events.
 * 
 * <p>
 * Register this listener in the web.xml configuration file. This will listen
 * for startup and shutdown events.
 * </p>
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    /**
     * The servlet container is stopping.
     */
    public void contextDestroyed(ServletContextEvent event) {
    	ShutdownManager.shutdown();
    }

    /**
     * Do nothing on startup.
     */
    public void contextInitialized(ServletContextEvent event) {

    }
    
}
