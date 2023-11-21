package io.ebean.config;

import io.avaje.config.Config;
import io.avaje.config.Configuration;

import java.util.Properties;

/**
 * Configuration for the container that holds the Database instances.
 * <p>
 * Provides configuration for cluster communication (if clustering is used). The cluster communication is
 * used to invalidate appropriate parts of the L2 cache across the cluster.
 */
public class ContainerConfig {

  private boolean active;
  private String serviceName;
  private String namespace;
  private String podName;
  private int port;
  private Properties properties;
  private Configuration configuration;

  public ContainerConfig() {
    this.configuration = Config.asConfiguration();
    this.active = configuration.getBool("ebean.cluster.active", active);
    this.serviceName = configuration.getNullable("ebean.cluster.serviceName", serviceName);
    this.namespace = configuration.getNullable("ebean.cluster.namespace", namespace);
    this.podName = configuration.getNullable("ebean.cluster.podName", podName);
    this.port = configuration.getInt("ebean.cluster.port", 0);
  }

  /**
   * Return the service name.
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Set the service name.
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Return the namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Set the namespace.
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Return the pod name.
   */
  public String getPodName() {
    return podName;
  }

  /**
   * Set the pod name.
   */
  public void setPodName(String podName) {
    this.podName = podName;
  }

  /**
   * Return the port to use.
   */
  public int getPort() {
    return port;
  }

  /**
   * Set the port to use.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * Return true if clustering is active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Set to true for clustering to be active.
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * Return the deployment properties.
   */
  public Properties getProperties() {
    return properties != null ? properties : configuration.asProperties();
  }

  /**
   * Set the deployment properties.
   */
  public void setProperties(Properties properties) {
    this.properties = properties;
  }

}
