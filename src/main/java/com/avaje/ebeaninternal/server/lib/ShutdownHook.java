package com.avaje.ebeaninternal.server.lib;



/**
 * This is the ShutdownHook that gets added to Runtime.
 * It will try to shutdown the system cleanly when the JVM exits.
 * It is best to add your own shutdown hooks to StartStop.
 * 
 */
class ShutdownHook extends Thread {

	ShutdownHook() {
	}

//    /**
//     * Register this as a Shutdown hook with the Runtime.
//     */
//	public static void registerWithRuntime() {
//		
//		ShutdownHook hook = new ShutdownHook();
//		Runtime.getRuntime().addShutdownHook(hook);
//	}


	/**
	 * Fired by the JVM Runtime on shutdown.
	 */
	public void run() {
		ShutdownManager.shutdown();
	}

}; 
