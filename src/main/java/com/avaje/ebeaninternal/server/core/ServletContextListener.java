/**
 * Copyright (C) 2006  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
package com.avaje.ebeaninternal.server.core;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.server.lib.ShutdownManager;

/**
 * Listens for webserver server starting and stopping events.
 * 
 * <p>
 * Register this listener in the web.xml configuration file. This will listen
 * for startup and shutdown events.
 * </p>
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    private static final Logger logger = Logger.getLogger(ServletContextListener.class.getName());

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
