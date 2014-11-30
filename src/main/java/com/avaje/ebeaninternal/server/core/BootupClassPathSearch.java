package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.server.util.ClassPathSearch;
import com.avaje.ebeaninternal.server.util.ClassPathSearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Searches for interesting classes such as Entities, Embedded and ScalarTypes.
 */
public class BootupClassPathSearch {

	private static final Logger logger = LoggerFactory.getLogger(BootupClassPathSearch.class);

	private final Object monitor = new Object();

	private final ClassLoader classLoader;

	private final List<String> packages;

  private final List<String> jars;
	
	private BootupClasses bootupClasses;

  private final String classPathReaderClassName;

	/**
	 * Construct and search for interesting classes.
	 */
	public BootupClassPathSearch(ClassLoader classLoader, List<String> packages, List<String> jars, String classPathReaderClassName) {
		this.classLoader = (classLoader == null) ? getClass().getClassLoader() : classLoader;
		this.packages = packages;
		this.jars = jars;
    this.classPathReaderClassName = classPathReaderClassName;
	}

	public BootupClasses getBootupClasses() {
		synchronized (monitor) {
			
			if (bootupClasses == null){
				bootupClasses = search();
			}
			
			return bootupClasses;
		}
	}

	/**
	 * Search the classPath for the classes we are interested in.
	 */
	private BootupClasses search() {
		synchronized (monitor) {
			try {
				
				BootupClasses bc = new BootupClasses();

				long st = System.currentTimeMillis();

				ClassPathSearchFilter filter = createFilter();

				ClassPathSearch finder = new ClassPathSearch(classLoader, filter, bc, classPathReaderClassName);

				finder.findClasses();
				Set<String> jars = finder.getJarHits();
				Set<String> pkgs = finder.getPackageHits();

				long searchTime = System.currentTimeMillis() - st;

				String msg = "Classpath search hits in jars" + jars + " pkgs" + pkgs + "  searchTime[" + searchTime+ "]";
				logger.info(msg);

				return bc;

			} catch (Exception ex) {
				String msg = "Error in classpath search (looking for entities etc)";
				throw new RuntimeException(msg, ex);
			}
		}
	}

	private ClassPathSearchFilter createFilter() {

		ClassPathSearchFilter filter = new ClassPathSearchFilter();
		filter.addDefaultExcludePackages();

        if (packages != null && packages.size() > 0) {
            for (String packageName : packages) {
                filter.includePackage(packageName);
            }

	          // if they specified include packages, they don't want by default to include everything
	          filter.setDefaultPackageMatch(false);
        }

        if (jars != null && jars.size() > 0) {
            for (String jarName : jars) {
                filter.includeJar(jarName);
            }

	          // if they specified jars to specifically include, they don't want everything included
	          filter.setDefaultJarMatch(false);
        }

		return filter;
	}
}
