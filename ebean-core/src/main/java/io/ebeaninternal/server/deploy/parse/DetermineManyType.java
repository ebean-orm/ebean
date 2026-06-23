package io.ebeaninternal.server.deploy.parse;

import io.ebeaninternal.server.deploy.ManyType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Determine the Many Type for a property.
 */
final class DetermineManyType {

  ManyType manyType(Class<?> type) {
    if (type.equals(List.class)) {
      return ManyType.LIST;
    }
    if (type.equals(Set.class) || type.getCanonicalName().equals("java.util.SequencedSet")) {
      return ManyType.SET;
    }
    if (type.equals(Map.class) || type.getCanonicalName().equals("java.util.SequencedMap")) {
      return ManyType.MAP;
    }
    return null;
  }
}
