package com.avaje.ebeaninternal.server.lib;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.Monitor;

/**
 * The Thread Pool based on Daemon threads.
 * 
 * @author rbygrave
 */
public final class DaemonThreadPool extends ThreadPoolExecutor {

    private static final Logger logger = Logger.getLogger(DaemonThreadPool.class.getName());

    private final Monitor monitor = new Monitor();
    
    private final String namePrefix;
    
    private int shutdownWaitSeconds;
    
	/**
	 * Construct the DaemonThreadPool.
	 * 
	 * @param coreSize
	 *            the core size of the thread pool.
	 * @param keepAliveSecs
	 *            the time in seconds idle threads are keep alive
	 * @param shutdownWaitSeconds
	 *            the time in seconds allowed for the pool to shutdown nicely.
	 *            After this the pool is forced to shutdown.
	 */
    public DaemonThreadPool(int coreSize, long keepAliveSecs, int shutdownWaitSeconds, String namePrefix) {
        super(coreSize, coreSize, keepAliveSecs, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory(namePrefix));
        this.shutdownWaitSeconds = shutdownWaitSeconds;
        this.namePrefix = namePrefix;
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
                logger.fine("... DaemonThreadPool["+namePrefix+"] already shut down");
                return;
            }
            try {
                logger.fine("DaemonThreadPool["+namePrefix+"] shutting down...");
                super.shutdown();
                if (!super.awaitTermination(shutdownWaitSeconds, TimeUnit.SECONDS)) {
                    logger.info("DaemonThreadPool["+namePrefix+"] shut down timeout exceeded. Terminating running threads.");
                    super.shutdownNow();
                }

            } catch (Exception e) {
                String msg = "Error during shutdown of DaemonThreadPool["+namePrefix+"]";
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

