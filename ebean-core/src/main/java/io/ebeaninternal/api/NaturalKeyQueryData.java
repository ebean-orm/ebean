package io.ebeaninternal.api;

import io.ebean.Pairs;
import io.ebeaninternal.server.deploy.BeanNaturalKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Collects the data for processing the natural key cache processing.
 */
public class NaturalKeyQueryData<T> {

  private final BeanNaturalKey naturalKey;

  /**
   * Only one of IN or IN PAIRS is allowed.
   */
  private boolean hasIn;

  // IN Pairs clause - only one allowed
  private String inProperty0, inProperty1;
  private List<Pairs.Entry> inPairs;

  // IN clause - only one allowed
  private List<Object> inValues;
  private String inProperty;

  // normal EQ expressions
  private List<NaturalKeyEq> eqList;

  private NaturalKeySet set;

  private int hitCount;

  public NaturalKeyQueryData(BeanNaturalKey naturalKey) {
    this.naturalKey = naturalKey;
  }

  private boolean matchProperty(String propName) {
    return naturalKey.matchProperty(propName);
  }

  /**
   * Match for In Pairs expression. We only allow one IN clause.
   */
  public List<Pairs.Entry> matchInPairs(String property0, String property1, List<Pairs.Entry> inPairs) {
    if (hasIn) {
      // only 1 IN allowed (to project naturalIds)
      return null;
    }
    if (matchProperty(property0) && matchProperty(property1)) {
      this.hasIn = true;
      this.inProperty0 = property0;
      this.inProperty1 = property1;
      this.inPairs = new ArrayList<>(inPairs); // will be modified
      return this.inPairs;
    }
    return null;
  }

  /**
   * Match for IN expression. We only allow one IN clause.
   */
  public boolean matchIn(String propName, List<Object> inValues) {
    if (hasIn) {
      // only 1 IN allowed (to project naturalIds)
      return false;
    }
    if (matchProperty(propName)) {
      this.hasIn = true;
      this.inProperty = propName;
      this.inValues = inValues;
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
    if (inValues != null) {
      addInValues();
    } else if (inPairs != null) {
      addInPairs();
    } else {
      addEqualsKey();
    }
    return set;
  }

  private void addInPairs() {
    // a findList() with an IN Map clause so we project
    // for every IN value a natural key combination
    for (Pairs.Entry entry : inPairs) {
      set.add(new NaturalKeyEntryBasic(naturalKey, eqList, inProperty0, inProperty1, entry));
    }
  }

  private void addInValues() {
    if (eqList == null) {
      // a single property IN expression
      for (Object inValue : inValues) {
        set.add(new NaturalKeyEntrySimple(inValue));
      }
    } else {
      // IN expression + EQ expression(s)
      for (Object inValue : inValues) {
        set.add(new NaturalKeyEntryBasic(naturalKey, eqList, inProperty, inValue));
      }
    }
  }

  private void addEqualsKey() {
    if (eqList.size() == 1) {
      // a single property EQ expression
      set.add(new NaturalKeyEntrySimple(eqList.get(0).value));
    } else {
      set.add(new NaturalKeyEntryBasic(naturalKey, eqList));
    }
  }

  /**
   * Return true if the properties match the natural key properties.
   */
  private boolean matchProperties() {
    if (naturalKey.isSingleProperty()) {
      naturalKey.matchSingleProperty((inProperty != null) ? inProperty : eqList.get(0).property);
    }

    // multiple properties case
    Set<String> exprProps = new HashSet<>();
    if (inProperty != null) {
      exprProps.add(inProperty);
    }
    if (inProperty0 != null) {
      exprProps.add(inProperty0);
    }
    if (inProperty1 != null) {
      exprProps.add(inProperty1);
    }
    if (eqList != null) {
      for (NaturalKeyEq eq : eqList) {
        exprProps.add(eq.property);
      }
    }
    return naturalKey.matchMultiProperties(exprProps);
  }

  /**
   * Check that all the natural key properties are defined.
   */
  private boolean expressionCount() {
    int defined = (inValues == null) ? 0 : 1;
    defined += (inPairs == null) ? 0 : 2;
    defined += (eqList == null) ? 0 : eqList.size();
    return defined == naturalKey.length();
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
      removeKey(set.getInValue(hit.getKey()));
      beans.add(hit.getBean());
    }
    return beans;
  }

  private void removeKey(Object inValue) {
    if (inValues != null) {
      inValues.remove(inValue);
    } else if (inPairs != null) {
      //noinspection SuspiciousMethodCalls
      inPairs.remove(inValue);
    }
  }
}
