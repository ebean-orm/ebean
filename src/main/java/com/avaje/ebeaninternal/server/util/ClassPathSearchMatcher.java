package com.avaje.ebeaninternal.server.util;

/**
 * Defines interface for finding classes via a class path search.
 */
public interface ClassPathSearchMatcher {

	/**
	 * Return true if the class matches the specific search.
	 * <p>
	 * Note that the location in terms of jars and packages is noted and can be
	 * used to make future searches faster.
	 * </p>
	 */
	public boolean isMatch(Class<?> cls);
}
