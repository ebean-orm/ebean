package io.ebeaninternal.api;


import io.ebean.Pairs;
import io.ebeaninternal.server.deploy.BeanNaturalKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Natural key entry with name value pairs for each of the properties making up the key.
 */
class NaturalKeyEntryBasic implements NaturalKeyEntry {

  private final Map<String,Object> map = new HashMap<>();
  private final String key;
  private Object inValue;

  /**
   * Used when query query just has a series of EQ expressions (no IN clause).
   */
  NaturalKeyEntryBasic(BeanNaturalKey naturalKey, List<NaturalKeyEq> eqList) {
    load(eqList);
    this.key = calculateKey(naturalKey);
  }

  /**
   * Create when query uses an IN clause.
   */
  NaturalKeyEntryBasic(BeanNaturalKey naturalKey, List<NaturalKeyEq> eqList, String inProperty, Object inValue) {
    load(eqList);
    if (inProperty != null) {
      map.put(inProperty, inValue);
      this.inValue = inValue;
    }
    this.key = calculateKey(naturalKey);
  }

  /**
   * Create when query uses an IN PAIRS clause.
   */
  NaturalKeyEntryBasic(BeanNaturalKey naturalKey, List<NaturalKeyEq> eqList,
                        String inMapProperty0, String inMapProperty1, Pairs.Entry pair) {
    load(eqList);
    map.put(inMapProperty0, pair.getA());
    map.put(inMapProperty1, pair.getB());
    this.inValue = pair;
    this.key = calculateKey(naturalKey);
  }

  private void load(List<NaturalKeyEq> eqList) {
    if (eqList != null) {
      for (NaturalKeyEq eq : eqList) {
        map.put(eq.property, eq.value);
      }
    }
  }

  private String calculateKey(BeanNaturalKey naturalKey) {
    return naturalKey.calculateKey(map);
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public Object getInValue() {
    return inValue;
  }
}
