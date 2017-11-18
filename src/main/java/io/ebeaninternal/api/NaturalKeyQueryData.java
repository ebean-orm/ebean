package io.ebeaninternal.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects the data for processing the natural key cache processing.
 */
public class NaturalKeyQueryData<T> {

  private final String[] naturalKey;

  private Collection<?> inValues;

  private String inProperty;

  private List<NaturalKeyEq> eqList;

  private NaturalKeySet set;

  private int hitCount;

  public NaturalKeyQueryData(String[] naturalKey) {
    this.naturalKey = naturalKey;
  }

  private boolean matchProperty(String propName) {

    for (String key : naturalKey) {
      if (key.equals(propName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Match for IN expression. We only allow one IN clause.
   */
  public boolean matchIn(String propName, Collection<?> sourceValues) {
    if (inProperty != null) {
      // only 1 IN allowed (to project naturalIds)
      return false;
    }
    if (matchProperty(propName)) {
      this.inProperty = propName;
      this.inValues = sourceValues;
      return true;
    }
    return false;
  }

  /**
   * Match for an EQ expression.
   */
  public boolean matchEq(String propName, Object bindValue) {
    if (matchProperty(propName)) {
      if (eqList == null) {
        eqList = new ArrayList<>();
      }
      eqList.add(new NaturalKeyEq(propName, bindValue));
      return true;
    }
    return false;
  }

  /**
   * Build and return the set of natural keys we will use.
   */
  public NaturalKeySet buildKeys() {

    if (!expressionCount() || !matchProperties()) {
      return null;
    }

    this.set = new NaturalKeySet();
    if (inValues == null) {
      // only one - a findOne()
      set.add(new NaturalKeyEntry(naturalKey, eqList));

    } else {
      // a findList() with an IN clause so we project
      // for every IN value a natural key combination
      for (Object inValue : inValues) {
        set.add(new NaturalKeyEntry(naturalKey, eqList, inProperty, inValue));
      }
    }

    return set;
  }

  /**
   * Return true if the properties match the natural key properties.
   */
  private boolean matchProperties() {
    if (naturalKey.length == 1) {
      // simple single property case
      if (inProperty != null) {
        return inProperty.equals(naturalKey[0]);
      } else {
        return eqList.get(0).property.equals(naturalKey[0]);
      }
    }

    // multiple properties case
    Set<String> exprProps = new HashSet<>();
    if (inProperty != null) {
      exprProps.add(inProperty);
    }
    if (eqList != null) {
      for (NaturalKeyEq eq : eqList) {
        exprProps.add(eq.property);
      }
    }
    if (exprProps.size() != naturalKey.length) {
      return false;
    }
    for (String key : naturalKey) {
      if (!exprProps.remove(key)) {
        return false;
      }
    }

    return exprProps.isEmpty();
  }

  /**
   * Check that all the natural key properties are defined.
   */
  private boolean expressionCount() {

    int defined = (inValues == null) ? 0 : 1;
    defined += (eqList == null) ? 0 : eqList.size();
    return defined == naturalKey.length;
  }

  /**
   * Return the number of entries in the IN clause left remaining (to hit the DB with).
   */
  public boolean allHits() {
    return hitCount > 0
      && hitCount == set.size()
      && (inValues == null || inValues.isEmpty());
  }

  /**
   * Adjust the IN clause removing the hit entry.
   */
  public List<T> removeHits(BeanCacheResult<T> cacheResult) {

    List<BeanCacheResult.Entry<T>> hits = cacheResult.hits();
    this.hitCount = hits.size();

    List<T> beans = new ArrayList<>(hitCount);

    for (BeanCacheResult.Entry<T> hit : hits) {
      if (inValues != null) {
        Object naturalKey = hit.getKey();
        Object inValue = set.getInValue(naturalKey);
        inValues.remove(inValue);
      }
      beans.add(hit.getBean());
    }

    return beans;
  }
}
