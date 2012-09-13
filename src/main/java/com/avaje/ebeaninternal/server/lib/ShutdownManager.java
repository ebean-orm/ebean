/**
 *  Copyright (C) 2006  Robin Bygrave
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.avaje.ebeaninternal.server.lib;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebean.common.BootupEbeanManager;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.ebeaninternal.api.ClassUtil;
import com.avaje.ebeaninternal.server.lib.sql.DataSourceGlobalManager;
import com.avaje.ebeaninternal.server.lib.thread.ThreadPoolManager;

/**
 * Manages the shutdown of the Runtime.
 * <p>
 * Makes sure all the resources are shutdown properly and in order.
 * </p>
 */
public final class ShutdownManager {

	private static final Logger logger = Logger.getLogger(BackgroundThread.class.getName());

	static final Vector<Runnable> runnables = new Vector<Runnable>();

	static boolean stopping;

	static BootupEbeanManager serverFactory;
	
	static final ShutdownHook shutdownHook = new ShutdownHook();

	static boolean whyShutdown;
	
	static {
		// Register the Shutdown hook
		register();
		whyShutdown = GlobalProperties.getBoolean("debug.shutdown.why",false);
	}

	/**
	 * Disallow construction.
	 */
	private ShutdownManager() {
	}

	public static void registerServerFactory(BootupEbeanManager factory){
		serverFactory = factory;
	}
	/**
	 * Make sure the ShutdownManager is activated.
	 */
	public static void touch() {

	}

	/**
	 * Return true if the system is in the process of stopping.
	 */
	public static boolean isStopping() {
		synchronized (runnables) {
			return stopping;
		}
	}

	/**
	 * Deregister the Shutdown hook.
	 * <p>
	 * For running in a Servlet Container a redeploy will cause a shutdown, and
	 * for that case we need to make sure the shutdown hook is deregistered.
	 * </p>
	 */
	private static void deregister() {
		synchronized (runnables) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownHook);
			} catch (IllegalStateException ex) {
				if (!ex.getMessage().equals("Shutdown in progress")) {
					throw ex;
				}
			}
		}
	}

	/**
	 * Register the shutdown hook with the Runtime.
	 */
	private static void register() {
		synchronized (runnables) {
			try {
				Runtime.getRuntime().addShutdownHook(shutdownHook);
			} catch (IllegalStateException ex) {
				if (!ex.getMessage().equals("Shutdown in progress")) {
					throw ex;
				}
			}
		}
	}
	
	/**
	 * cleanup any resources as Runtime is stopping.
	 * <p>
	 * <ul>
	 * <li>Run the application specific shutdown runnable
	 * <li>Run any other registered shutdown runnable
	 * <li>Deregister from the cluster if required
	 * <li>Shutdown Thread pools
	 * <li>Shutdown any database connection pools
	 * </ul>
	 * </p>
	 */
	public static void shutdown() {
		synchronized (runnables) {
			if (stopping) {
				// Already run shutdown...
				return;
			}
			
			if (whyShutdown){
				try {
					throw new RuntimeException("debug.shutdown.why=true ...");
				} catch(Throwable e){
					logger.log(Level.WARNING, "Stacktrace showing why shutdown was fired", e);
				}
			}
			
			stopping = true;
			//logger.info("Stopping [" + SystemProperties.getContextName() + "]");

			deregister();
	
			// stop the BackgroundThread
			BackgroundThread.shutdown();

			String shutdownRunner = GlobalProperties.get("system.shutdown.runnable", null);
			if (shutdownRunner != null) {
				try {
				    Runnable r = (Runnable)ClassUtil.newInstance(shutdownRunner);
					r.run();
				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
				}
			}

			// shutdown any registered runnable

			Enumeration<Runnable> e = runnables.elements();
			while (e.hasMoreElements()) {
				try {
					Runnable r = (Runnable) e.nextElement();
					r.run();
				} catch (Exception ex) {
					logger.log(Level.SEVERE, null, ex);
					ex.printStackTrace();
				}
			}
			try {
				// shutdown order is important!
				// CronManager is ok

				if (serverFactory != null){
					serverFactory.shutdown();
				}

				ThreadPoolManager.shutdown();

				DataSourceGlobalManager.shutdown();
				
				boolean dereg = GlobalProperties.getBoolean("datasource.deregisterAllDrivers", false);
				if (dereg){
					deregisterAllJdbcDrivers();
				}

			} catch (Exception ex) {
				String msg = "Shutdown Exception: "+ ex.getMessage();
				System.err.println(msg);
				ex.printStackTrace();
				try {
					logger.log(Level.SEVERE, null, ex);
				} catch (Exception exc) {
					String ms = "Error Logging error to the Log. It may be shutting down.";
					System.err.println(ms);
					exc.printStackTrace();
				}
			}
		}
	}

	private static void deregisterAllJdbcDrivers() {
	    // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrto this class
	    Enumeration<Driver> drivers = DriverManager.getDrivers();
	    while (drivers.hasMoreElements()) {
	        Driver driver = drivers.nextElement();
	        try {
	            DriverManager.deregisterDriver(driver);
	            logger.log(Level.INFO, String.format("Deregistering jdbc driver: %s", driver));
	        } catch (SQLException e) {
	        	logger.log(Level.SEVERE, String.format("Error deregistering driver %s", driver), e);
	        }
	    }
	}
	
	/**
	 * Register a runnable to be executed when the system is shutdown. Note that
	 * runnables registered here are shutdown before any thread pools or
	 * DataSource pools are shutdown.
	 */
	public static void register(Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}
}
