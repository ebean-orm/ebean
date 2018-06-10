package io.ebeaninternal.server.core.bootup;

import io.ebean.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
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

  private final ClassLoader classLoader;

  /**
   * Create with a classLoader to use to read the ebean.mf resources.
   */
  static ManifestReader create(ClassLoader classLoader) {
    return new ManifestReader(classLoader);
  }

  private ManifestReader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  /**
   * Read the packages from ebean.mf manifest files found as resources.
   */
  ManifestReader read(String resourcePath) {
    read(classLoader, resourcePath);
    return this;
  }

  /**
   * Return all the entityPackages read.
   */
  Set<String> entityPackages() {
    return packageSet;
  }

  /**
   * Read all the specific manifest files and return the set of packages containing type query beans.
   */
  private Set<String> read(ClassLoader classLoader, String resourcePath) {

    try {
      Enumeration<URL> resources = classLoader.getResources(resourcePath);
      while (resources.hasMoreElements()) {
        try (InputStream is = resources.nextElement().openStream()) {
          read(new Manifest(is));
        }
      }
    } catch (IOException e) {
      logger.warn("Error reading " + resourcePath + " manifest resources", e);
    }
    return packageSet;
  }

  /**
   * Read the entity packages from the manifest.
   */
  private void read(Manifest manifest) throws IOException {

    Attributes attributes = manifest.getMainAttributes();
    String agentOnlyUse = attributes.getValue("agent-use-only");
    if (agentOnlyUse == null || !"true".equalsIgnoreCase(agentOnlyUse.trim())) {
      add(attributes.getValue("packages"));
      add(attributes.getValue("entity-packages"));
    }
  }

  /**
   * Collect each individual package splitting by delimiters.
   */
  private void add(String packages) {
    if (packages != null) {
      Collections.addAll(packageSet, StringHelper.splitNames(packages));
    }
  }
}
