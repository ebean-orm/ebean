package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.ClassPathSearchService;
import com.avaje.ebeaninternal.server.util.ClassPathSearchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Searches for interesting classes such as Entities, Embedded and ScalarTypes.
 */
public class BootupClassPathSearch {

  private static final Logger logger = LoggerFactory.getLogger(BootupClassPathSearch.class);

  private final Object monitor = new Object();

  private final ClassLoader classLoader;

  private final List<String> packages;

  private final List<String> jars;

  private List<ClassPathSearchService> classPathSearchServices;

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

    loadAndInitializeClassPathSearchServices();
  }

  public BootupClasses getBootupClasses() {
    synchronized (monitor) {

      if (bootupClasses == null) {
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

        Set<String> foundJars = new HashSet<String>();
        Set<String> foundPkgs = new HashSet<String>();

        for (ClassPathSearchService finder : this.classPathSearchServices) {
          finder.init(classLoader, filter, bc, classPathReaderClassName);
          finder.findClasses();
          foundJars.addAll(finder.getJarHits());
          foundPkgs.addAll(finder.getPackageHits());
        }

        long searchTime = System.currentTimeMillis() - st;

        logger.info("Classpath search hits in jars {} pkgs {} searchTime [{}]", foundJars, foundPkgs, searchTime);
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

  /**
   * Load and initialize all ClassPathSearchServices
   */
  private void loadAndInitializeClassPathSearchServices() {
    if (this.classPathSearchServices == null) {
      this.classPathSearchServices = new ArrayList<ClassPathSearchService>();
    }

    for (ClassPathSearchService searchService : ServiceLoader.load(ClassPathSearchService.class, classLoader)) {
      this.classPathSearchServices.add(searchService);
    }
  }
}
