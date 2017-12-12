package io.ebeaninternal.api;


import io.ebean.Pairs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Natural key entry with name value pairs for each of the properties making up the key.
 */
public class NaturalKeyEntry {

  private Map<String,Object> map = new HashMap<>();
  private Object key;
  private Object inValue;

  /**
   * Used when query query just has a series of EQ expressions (no IN clause).
   */
  public NaturalKeyEntry(String[] naturalKey, List<NaturalKeyEq> eqList) {
    load(eqList);
    this.key = calculateKey(naturalKey);
  }

  /**
   * Create when query uses an IN clause.
   */
  public NaturalKeyEntry(String[] naturalKey, List<NaturalKeyEq> eqList, String inProperty, Object inValue) {
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
  public NaturalKeyEntry(String[] naturalKey, List<NaturalKeyEq> eqList,
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

  private Object calculateKey(String[] naturalKey) {

    if (naturalKey.length == 1) {
      return map.get(naturalKey[0]);
    }

    StringBuilder sb = new StringBuilder();
    for (String key : naturalKey) {
      sb.append(map.get(key)).append(";");
    }

    return sb.toString();
  }

  /**
   * Return the natural cache key (String concatenation of values).
   */
  public Object key() {
    return key;
  }

  /**
   * Return the inValue (used to remove from IN clause of original query).
   */
  public Object getInValue() {
    return inValue;
  }
}
