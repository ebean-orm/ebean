package io.ebean;

import java.io.Serializable;
/**
 * Holds a distinct value with it's count.
 * (Used with {@link Query#findSingleAttributeList()} and {@link Query#setCountDistinct(CountDistinctOrder)}.)
 * @author Roland Praml, FOCONIS AG
 */
public class CountedValue<A> implements Serializable {
  private static final long serialVersionUID = -2267971668356749695L;

  private final A value;
  private final long count;

  public CountedValue(A value, long count) {
    this.value = value;
    this.count = count;
  }

  public long getCount() {
    return count;
  }
  
  public A getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return count + ": " + value;
  }
  
}
