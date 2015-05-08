package com.avaje.ebeaninternal.server.util;

/**
 * Helper for checking the state of the application is correct.
 */
public class InternalAssert {

	/**
	 * Throws an IllegalStateException if o is null.
	 */
	public static void notNull(Object o, String msg){
		if (o == null){
			throw new IllegalStateException(msg);
		}
	}
	
	/**
	 * Throws an IllegalStateException if b is not true.
	 */
	public static void isTrue(boolean b, String msg){
		if (!b){
			throw new IllegalStateException(msg);
		}
	}
}
