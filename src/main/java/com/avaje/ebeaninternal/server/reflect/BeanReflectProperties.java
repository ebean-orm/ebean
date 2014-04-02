package com.avaje.ebeaninternal.server.reflect;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BeanReflectProperties {

  private final Map<String,Integer> propertyIndexMap = new HashMap<String,Integer>();
  
  private final String[] props;
  
  public BeanReflectProperties(Class<?> clazz) {
    this.props = getProperties(clazz);
    for (int i=0; i<props.length; i++) {
      propertyIndexMap.put(props[i], Integer.valueOf(i));
    }  
  }
  
  public String[] getProperties() {
    return props;
  }
  
  public String toString() {
    return Arrays.toString(props);
  }

  public Integer getPropertyIndex(String property) {
    return propertyIndexMap.get(property);
  }
  
  private String[] getProperties(Class<?> clazz) {
    try {
      Field field = clazz.getField("_ebean_props");
      return (String[]) field.get(null);

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
