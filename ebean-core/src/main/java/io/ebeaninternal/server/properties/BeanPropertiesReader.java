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

  private String[] concat(String[] arr1, String[] arr2) {
    String[] ret = new String[arr1.length + arr2.length];
    System.arraycopy(arr1, 0, ret, 0, arr1.length);
    System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
    return ret;
  }

  private String[] getProperties(Class<?> clazz) {
    try {
      Field field = clazz.getField("_ebean_props");
      String[] props = (String[]) field.get(null);

      for (ExtensionAccessor extension : ExtensionAccessors.read(clazz)) {
        props = concat(props, extension.getProperties());
      }
      return props;
    } catch (Exception e) {
      throw new IllegalStateException("Error getting _ebean_props field on type " + clazz, e);
    }
  }
}
