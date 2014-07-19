package com.avaje.ebeaninternal.server.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Used to reduce the classes searched by excluding jars and packages.
 */
public class ClassPathSearchFilter {

	private static final String COM_AVAJE_EBEANINTERNAL_SERVER_BEAN = "com.avaje.ebeaninternal.server.bean";

  private static final String COM_AVAJE_EBEAN_META = "com.avaje.ebean.meta";

  private boolean defaultPackageMatch = true;

	private boolean defaultJarMatch = false;

	private String ebeanJarPrefix = "ebean";

	private HashSet<String> includePackageSet = new HashSet<String>();

	private HashSet<String> excludePackageSet = new HashSet<String>();

	private HashSet<String> includeJarSet = new HashSet<String>();

	private HashSet<String> excludeJarSet = new HashSet<String>();

	public ClassPathSearchFilter() {
		addDefaultExcludePackages();
	}

	/**
	 * Set the name of the ebean jar file. Used with class path search to find
	 * the "Meta" entity beans that are contained within the ebean jar.
	 * <p>
	 * You only need to set this if the ebean jar file name starts with
	 * something other than ebean.
	 * </p>
	 */
	public void setEbeanJarPrefix(String ebeanJarPrefix) {
		this.ebeanJarPrefix = ebeanJarPrefix;
	}

	/**
	 * Return the explicit packages that should be searched.
	 */
	public Set<String> getIncludePackages() {
		return includePackageSet;
	}

	/**
	 * Add some packages which by default will be excluded from a search.
	 * <p>
	 * This includes java, javax, etc.
	 * </p>
	 * <p>
	 * This is not used when the includePackages is set, but can speed a search
	 * when includePackages has not been set.
	 * </p>
	 */
	public void addDefaultExcludePackages() {
		excludePackage("sun");
		excludePackage("com.sun");
		excludePackage("java");
		excludePackage("javax");
		excludePackage("junit");
		excludePackage("org.w3c");
		excludePackage("org.xml");
		excludePackage("org.apache");
		excludePackage("com.mysql");
		excludePackage("oracle.jdbc");
		excludePackage("com.microsoft.sqlserver");
		excludePackage("com.avaje.ebean");
		excludePackage("com.avaje.lib");
	}

	/**
	 * Clear all entries from the exclude packages list.
	 * <p>
	 * This includes the entries added by addDefaultExcludePackages() which is
	 * done on construction.
	 * </p>
	 */
	public void clearExcludePackages() {
		excludePackageSet.clear();
	}

	/**
	 * Set the default for jar matching when a jar is neither explicitly
	 * included or excluded.
	 */
	public void setDefaultJarMatch(boolean defaultJarMatch) {
		this.defaultJarMatch = defaultJarMatch;
	}

	/**
	 * Set the default for package matching when a package is neither explicitly
	 * included or excluded.
	 */
	public void setDefaultPackageMatch(boolean defaultPackageMatch) {
		this.defaultPackageMatch = defaultPackageMatch;
	}

	/**
	 * Add a package to explicitly include in the search.
	 */
	public void includePackage(String pckgName) {
		includePackageSet.add(pckgName);
	}

	/**
	 * Add a package to explicitly exclude in the search.
	 */
	public void excludePackage(String pckgName) {
		excludePackageSet.add(pckgName);
	}

	/**
	 * Add a jar to explicitly exclude in the search.
	 */
	public void excludeJar(String jarName) {
		excludeJarSet.add(jarName);
	}

	/**
	 * Add a jar to explicitly include in the search.
	 */
	public void includeJar(String jarName) {
		includeJarSet.add(jarName);
	}

	/**
	 * Return true if the package should be included in the search.
	 */
	public boolean isSearchPackage(String packageName) {
		// special case... "meta" entity beans.
		if (COM_AVAJE_EBEAN_META.equals(packageName)) {
			return true;
		}
		// special case... BeanFinders etc for "meta" beans.
		if (COM_AVAJE_EBEANINTERNAL_SERVER_BEAN.equals(packageName)) {
			return true;
		}
		if (includePackageSet != null && !includePackageSet.isEmpty()){
	        return containedIn(includePackageSet, packageName);
		}
		if (containedIn(excludePackageSet, packageName)) {
			return false;
		}
		return defaultPackageMatch;
	}

	/**
	 * Return true if the jar should be included in the search.
	 */
	public boolean isSearchJar(String jarName) {
		if (jarName.startsWith(ebeanJarPrefix)) {
			return true;
		}

		if (containedIn(includeJarSet, jarName)) {
			return true;
		}

		if (containedIn(excludeJarSet, jarName)) {
			return false;
		}
		return defaultJarMatch;
	}

	/**
	 * Helper method to determine is a match is contained in the set.
	 */
	protected boolean containedIn(HashSet<String> set, String match) {
		if (set.contains(match)) {
			return true;
		}
		for (String val : set) {
			if (match.contains(val)) {
				return true;
			}
		}
		return false;
	}

}
