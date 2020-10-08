package io.ebeaninternal.server.deploy;

import java.util.Map;
import java.util.Set;

/**
 * Natural key for a bean type.
 */
public class BeanNaturalKey {

  private final String[] naturalKey;
  private final BeanProperty[] props;

  BeanNaturalKey(String[] naturalKey, BeanProperty[] props) {
    this.naturalKey = naturalKey;
    this.props = props;
  }

  public int length() {
    return naturalKey.length;
  }

  /**
   * Return true if the property name is part of the natural key.
   */
  public boolean matchProperty(String propName) {
    for (String key : naturalKey) {
      if (key.equals(propName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Return true if this is a single property natural key.
   */
  public boolean isSingleProperty() {
    return props.length == 1;
  }

  /**
   * Return true if the given propertyName is our natural key property.
   */
  public boolean matchSingleProperty(String propertyName) {
    return naturalKey[0].equals(propertyName);
  }

  /**
   * Return true if all the properties match our natural key.
   */
  public boolean matchMultiProperties(Set<String> expressionProperties) {
    if (expressionProperties.size() != naturalKey.length) {
      return false;
    }
    for (String key : naturalKey) {
      if (!expressionProperties.remove(key)) {
        return false;
      }
    }
    return expressionProperties.isEmpty();
  }

  /**
   * Return the cache key given the bind values.
   *
   * @param map The bind values for the properties.
   */
  public String calculateKey(Map<String, Object> map) {
    StringBuilder sb = new StringBuilder();
    for (BeanProperty prop : props) {
      sb.append(prop.naturalKeyVal(map)).append(";");
    }
    return sb.toString();
  }
}
