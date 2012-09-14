package com.avaje.ebeaninternal.server.lib;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.Monitor;

/**
 * Daemon based ScheduleThreadPool.
 * <p>
 * Uses Daemon threads and hooks into shutdown event.
 * </p>
 * 
 * @author rbygrave
 */
public final class DaemonScheduleThreadPool extends ScheduledThreadPoolExecutor {

    private static final Logger logger = Logger.getLogger(DaemonScheduleThreadPool.class.getName());

    private final Monitor monitor = new Monitor();

    private int shutdownWaitSeconds;

	/**
	 * Construct the DaemonScheduleThreadPool.
	 */
    public DaemonScheduleThreadPool(int coreSize, int shutdownWaitSeconds, String namePrefix) {
        super(coreSize, new DaemonThreadFactory(namePrefix));
        this.shutdownWaitSeconds = shutdownWaitSeconds;

        // we want to shutdown nicely when either the web application stops.
        // Adding the JVM shutdown hook as a safety (and when not run in tomcat)
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
    
    /**
     * Shutdown this thread pool nicely if possible.
     * <p>
     * This will wait a maximum of 20 seconds before terminating any threads
     * still working.
     * </p>
     */
    public void shutdown() {
        synchronized (monitor) {
            if (super.isShutdown()) {
                logger.fine("... DaemonScheduleThreadPool already shut down");
                return;
            }
            try {
                logger.fine("DaemonScheduleThreadPool shutting down...");
                super.shutdown();
                if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
                    logger.info("ScheduleService shut down timeout exceeded. Terminating running threads.");
                    super.shutdownNow();
                }

            } catch (Exception e) {
                String msg = "Error during shutdown";
                logger.log(Level.SEVERE, msg, e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Fired by the JVM Runtime shutdown.
     */
    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            shutdown();
        }
    };
}

