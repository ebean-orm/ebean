package com.avaje.ebeaninternal.server.core.bootup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reads all the META-INF/ebean.mf resources with the package locations of entity beans.
 */
class ManifestReader {

  private static final Logger logger = LoggerFactory.getLogger(ManifestReader.class);

  private final Set<String> packageSet = new HashSet<>();

  /**
   * Read the packages from ebean.mf manifest files found as resources.
   */
  static Set<String> readManifests(ClassLoader classLoader, String resourcePath)  {
    return new ManifestReader().read(classLoader, resourcePath);
  }

  /**
   * Read all the specific manifest files and return the set of packages containing type query beans.
   */
  private Set<String> read(ClassLoader classLoader, String resourcePath)  {

    try {
      Enumeration<URL> resources = classLoader.getResources(resourcePath);
      while (resources.hasMoreElements()) {
        InputStream is = resources.nextElement().openStream();
        try {
          read(new Manifest(is));
        } finally {
          close(is);
        }
      }
    } catch (IOException e) {
      logger.warn("Error reading META-INF/ebean.mf manifest resources", e);
    }
    return packageSet;
  }

  private void close(InputStream is) {
    try {
      is.close();
    } catch (IOException e) {
      logger.warn("Error closing manifest InputStream", e);
    }
  }

  /**
   * Read the packages from the manifest.
   */
  private void read(Manifest manifest) throws IOException {

    Attributes attributes = manifest.getMainAttributes();
    String packages = attributes.getValue("packages");
    if (packages != null) {
      add(packages);
    }
  }

  /**
   * Collect each individual package splitting by delimiters.
   */
  private void add(String packages) {
    String[] split = packages.split(",|;| ");
    for (String aSplit : split) {
      String pkg = aSplit.trim();
      if (!pkg.isEmpty()) {
        packageSet.add(pkg);
      }
    }
  }
}
