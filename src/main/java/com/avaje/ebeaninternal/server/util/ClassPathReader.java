package com.avaje.ebeaninternal.server.util;

/**
 * Get the paths used to search for entity beans etc.
 * <p>
 * Typically this will return a URL[] of the classpath.
 * </p>
 */
public interface ClassPathReader {

	/**
	 * Return the paths in the classpath to search for entity beans etc.
	 * <p>
	 * This will typically return a URL[] or String[] of the entries in the
	 * class path.
	 * </p>
	 */
	public Object[] readPath(ClassLoader classLoader);
}
