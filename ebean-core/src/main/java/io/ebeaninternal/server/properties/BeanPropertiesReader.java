package io.ebeaninternal.server.properties;

import io.ebean.bean.ExtensionAccessor;
import io.ebean.bean.ExtensionAccessors;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Determines the properties on a given bean.
 */
public final class BeanPropertiesReader {

  private final Map<String, Integer> propertyIndexMap = new HashMap<>();

  public BeanPropertiesReader(String[] props) {
    for (int i = 0; i < props.length; i++) {
      propertyIndexMap.put(props[i], i);
    }
  }

  public Integer propertyIndex(String property) {
    return propertyIndexMap.get(property);
  }
  
}
