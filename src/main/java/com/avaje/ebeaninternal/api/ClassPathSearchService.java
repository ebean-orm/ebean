package com.avaje.ebeaninternal.api;

import com.avaje.ebeaninternal.server.util.ClassPathSearchFilter;
import com.avaje.ebeaninternal.server.util.ClassPathSearchMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This interface allows us to have more than one ClassPathSearch
 * to scan the resources by a customized way.
 *
 * @author Kefeng Deng (deng@51any.com)
 */
public interface ClassPathSearchService {

	/**
	 * Initialize this ClassPathSearchService with given parameters
	 *
	 * @param classLoader is current classLoader
	 * @param filter is filter
	 * @param matcher
	 * @param classPathReaderClassName
	 */
	void init(ClassLoader classLoader, ClassPathSearchFilter filter, ClassPathSearchMatcher matcher, String classPathReaderClassName);

	/**
	 * Searches the class path for all matching classes.
	 *
	 * @return a collection of all matching classes
	 * @throws IOException if a resource is un-reachable
	 */
	List<Class<?>> findClasses() throws IOException;

	/**
	 * Return the set of jars that contained classes that matched.
	 */
	Set<String> getJarHits();

	/**
	 * Return the set of packages that contained classes that matched.
	 */
	Set<String> getPackageHits();

}
