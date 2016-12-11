package io.ebeaninternal.server.deploy.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects Deployment information on Embedded beans.
 * <p>
 * Typically collects the overridden column names mapped
 * to the Embedded bean.
 * </p>
 */
public class DeployBeanEmbedded {

  /**
   * A map of property names to dbColumns.
   */
  private final Map<String, String> propMap = new HashMap<>();

  /**
   * Set a Map of property names to dbColumns.
   */
  public void putAll(Map<String, String> propertyColumnMap) {
    propMap.putAll(propertyColumnMap);
  }

  /**
   * Return a map of property names to dbColumns.
   */
  public Map<String, String> getPropertyColumnMap() {
    return propMap;
  }


}
