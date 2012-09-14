package com.avaje.ebeaninternal.server.lib;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A general background thread that runs registered tasks periodically.
 * <p>
 * Several features such as CacheManager, DataSourceManager, ThreadPoolManager
 * require tasks to be undertaken periodically. Instead of each having their own
 * background thread they register runnables with this one.
 * </p>
 * <p>
 * SystemProperties:<br>
 * 
 * <pre><code>
 *   ## initially sleep for 5 seconds before starting 
 *   backgroundthread.initialsleep=5
 * </code></pre>
 * 
 * </p>
 * 
 * @see BackgroundRunnable
 */
public final class BackgroundThread {

	private static final Logger logger = Logger.getLogger(BackgroundThread.class.getName());

    private static final BackgroundThread me = new BackgroundThread();

	/**
	 * The list of Runnable tasks.
	 */
	private Vector<BackgroundRunnable> list = new Vector<BackgroundRunnable>();

	/**
	 * Used to synchronize the list.
	 */
	private final Object monitor = new Object();

	/**
	 * The underlying background thread.
	 */
	private final Thread thread;

	/**
	 * Wakes every second to look for tasks to run.
	 */
	private long sleepTime = 1000;

	/**
	 * The number of times a task is run.
	 */
	private long count;

	/**
	 * The time it takes to run the tasks.
	 */
	private long exeTime;

	/**
	 * Set when shutting down.
	 */
	private boolean stopped;

	/**
	 * Used to shutdown nicely.
	 */
	private Object threadMonitor = new Object();

	private BackgroundThread() {

		thread = new Thread(new Runner(), "EbeanBackgroundThread");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Register a Runnable to execute every freqInSecs seconds.
	 */
	public static void add(int freqInSecs, Runnable runnable) {
		add(new BackgroundRunnable(runnable, freqInSecs));
	}

	/**
	 * Register a Runnable to execute every freqInSecs seconds.
	 */
	public static void add(BackgroundRunnable backgroundRunnable) {
		me.addTask(backgroundRunnable);
	}

	/**
	 * Stop the service.
	 */
	public static void shutdown() {
		me.stop();
	}

	/**
	 * Return the registered BackgroundRunnable objects.
	 */
	public static Iterator<BackgroundRunnable> runnables() {
		synchronized (me.monitor) {
			return me.list.iterator();
		}
	}

	private void addTask(BackgroundRunnable backgroundRunnable) {
		synchronized (monitor) {
			list.add(backgroundRunnable);
		}
	}

	/**
	 * Stop the thread nicely. This will wait a maximum of 10 seconds for
	 * current work to be finished.
	 */
	private void stop() {

		stopped = true;
		synchronized (threadMonitor) {
			try {
				threadMonitor.wait(10000);
			} catch (InterruptedException e) {
				;
			}
		}
		// thread = null;
	}

	private class Runner implements Runnable {

		/**
		 * Run the registered tasks periodically.
		 */
		public void run() {

			if (ShutdownManager.isStopping()) {
				return;
			}
			
			while (!stopped) {
				try {

					long actualSleep = sleepTime - exeTime;
					if (actualSleep < 0) {
						actualSleep = sleepTime;
					}
					Thread.sleep(actualSleep);
					synchronized (monitor) {
						runJobs();
					}

				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, null, e);
				}
			}

			// Tell Stop() we have shut ourselves down successfully
			synchronized (threadMonitor) {
				threadMonitor.notifyAll();
			}
		}

		private void runJobs() {

			long startTime = System.currentTimeMillis();

			// call trim on each cache
			Iterator<BackgroundRunnable> it = list.iterator();
			while (it.hasNext()) {
				BackgroundRunnable bgr = (BackgroundRunnable) it.next();
				if (bgr.isActive()) {

					int freqInSecs = bgr.getFreqInSecs();

					if (count % freqInSecs == 0) {
						Runnable runable = bgr.getRunnable();
						if (bgr.runNow(startTime)){
							bgr.runStart();
							if (logger.isLoggable(Level.FINER)) {
								String msg = count + " BGRunnable running ["
										+ runable.getClass().getName() + "]";
								logger.finer(msg);
							}
	
							runable.run();
							bgr.runEnd();
						}
					}
				}
			}
			exeTime = System.currentTimeMillis() - startTime;
			count++;

			if (count == 86400) {
				// reset count back to zero every day
				count = 0;
			}
		}
	}

	public String toString() {
		synchronized (monitor) {
			StringBuffer sb = new StringBuffer();

			Iterator<BackgroundRunnable> it = runnables();
			while (it.hasNext()) {
				BackgroundRunnable bgr = it.next();
				sb.append(bgr);
			}

			return sb.toString();
		}
	}

}
