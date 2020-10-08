package io.ebeaninternal.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NaturalKeySet {


  private final Map<Object, NaturalKeyEntry> map = new LinkedHashMap<>();

  NaturalKeySet() {
  }

  public int size() {
    return map.size();
  }

  public void add(NaturalKeyEntry entry) {
    map.put(entry.key(), entry);
  }

  public Set<Object> keys() {
    return map.keySet();
  }

  Object getInValue(Object naturalKey) {
    return map.get(naturalKey).getInValue();
  }
}
