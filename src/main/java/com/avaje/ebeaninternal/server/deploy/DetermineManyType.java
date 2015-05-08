package com.avaje.ebeaninternal.server.deploy;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Determine the Many Type for a property.
 */
public class DetermineManyType {

  public DetermineManyType() {
  }

  public ManyType getManyType(Class<?> type) {
    if (type.equals(List.class)) {
      return ManyType.JAVA_LIST;
    }
    if (type.equals(Set.class)) {
      return ManyType.JAVA_SET;
    }
    if (type.equals(Map.class)) {
      return ManyType.JAVA_MAP;
    }
    return null;
  }
}
