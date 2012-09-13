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
