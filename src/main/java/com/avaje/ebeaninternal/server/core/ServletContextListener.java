package com.avaje.ebeaninternal.server.core;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for webserver server starting and stopping events.
 * 
 * <p>
 * Register this listener in the web.xml configuration file. This will listen
 * for startup and shutdown events.
 * </p>
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ServletContextListener.class);

    /**
     * The servlet container is stopping.
     */
    public void contextDestroyed(ServletContextEvent event) {
    	ShutdownManager.shutdown();
    }

    /**
     * The servlet container is starting. 
     * <p>
     * Initialise the properties file using SystemProperties.initWebapp();
     * and start Ebean.
     * </p>
     */
    public void contextInitialized(ServletContextEvent event) {

        try {
            ServletContext servletContext = event.getServletContext();
            GlobalProperties.setServletContext(servletContext);

            if (servletContext != null) {
                String servletRealPath = servletContext.getRealPath("");
                GlobalProperties.put("servlet.realpath", servletRealPath);
                logger.info("servlet.realpath=[" + servletRealPath + "]");
            }            
 
            Ebean.getServer(null);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
