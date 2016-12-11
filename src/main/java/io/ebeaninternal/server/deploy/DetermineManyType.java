package io.ebeaninternal.server.deploy;

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
      return ManyType.LIST;
    }
    if (type.equals(Set.class)) {
      return ManyType.SET;
    }
    if (type.equals(Map.class)) {
      return ManyType.MAP;
    }
    return null;
  }
}
