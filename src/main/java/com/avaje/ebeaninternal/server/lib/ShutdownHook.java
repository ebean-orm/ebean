package com.avaje.ebeaninternal.server.lib;



/**
 * This is the ShutdownHook that gets added to Runtime.
 * It will try to shutdown the system cleanly when the JVM exits.
 * It is best to add your own shutdown hooks to StartStop.
 */
class ShutdownHook extends Thread {

	ShutdownHook() {
	}

	/**
	 * Fired by the JVM Runtime on shutdown.
	 */
	public void run() {
		ShutdownManager.shutdown();
	}

}; 
