package io.ebeaninternal.server.deploy.meta;

import javax.persistence.Column;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects Deployment information on Embedded beans.
 * <p>
 * Typically collects the overridden column names mapped
 * to the Embedded bean.
 */
public class DeployBeanEmbedded {

  /**
   * A map of property names to dbColumns.
   */
  private final Map<String, Column> propMap = new HashMap<>();

  /**
   * Set a Map of property names to dbColumns.
   */
  public void putAll(Map<String, Column> propertyColumnMap) {
    propMap.putAll(propertyColumnMap);
  }

  /**
   * Return a map of property names to dbColumns.
   */
  public Map<String, Column> getPropertyColumnMap() {
    return propMap;
  }

}
