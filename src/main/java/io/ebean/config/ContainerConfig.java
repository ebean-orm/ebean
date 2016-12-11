package io.ebean.config;

import java.util.Properties;

/**
 * Configuration for the container that holds the EbeanServer instances.
 * <p>
 * Provides configuration for cluster communication (if clustering is used). The cluster communication is
 * used to invalidate appropriate parts of the L2 cache across the cluster.
 */
public class ContainerConfig {

  protected boolean clusterActive;

  protected Properties properties;

  /**
   * Return true if clustering is active.
   */
  public boolean isClusterActive() {
    return clusterActive;
  }

  /**
   * Set to true for clustering to be active.
   */
  public void setClusterActive(boolean clusterActive) {
    this.clusterActive = clusterActive;
  }

  /**
   * Return the deployment properties.
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Set the deployment properties.
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  /**
   * Load the settings from properties.
   */
  public void loadFromProperties(Properties properties) {
    this.properties = properties;
    this.clusterActive = getProperty(properties, "ebean.cluster.active", clusterActive);
  }

  /**
   * Return the boolean property setting.
   */
  protected boolean getProperty(Properties properties, String key, boolean defaultValue) {
    return "true".equalsIgnoreCase(properties.getProperty(key, Boolean.toString(defaultValue)));
  }

}
