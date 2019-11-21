package io.ebean.bean;

import java.io.Serializable;

/**
 * Represents a "origin" of an ORM object graph. This combines the call stack
 * and query plan hash.
 * <p>
 * The call stack is included so that the query can have different tuned fetches
 * for each unique call stack. For example, a query to fetch a customer could be
 * called by three different methods and each can be treated as a separate
 * origin point (and autoTune can tune each one separately).
 * </p>
 */
public final class ObjectGraphOrigin implements Serializable {

  private static final long serialVersionUID = 410937765287968708L;

  private final CallOrigin callOrigin;

  private final String beanType;

  private final int queryHash;

  private final String key;

  public ObjectGraphOrigin(int queryHash, CallOrigin callOrigin, String beanType) {
    this.callOrigin = callOrigin;
    this.beanType = beanType;
    this.queryHash = queryHash;
    this.key = callOrigin.getOriginKey(queryHash);
  }

  /**
   * The key includes the queryPlan hash and the callStack hash. This becomes
   * the unique identifier for a query point.
   */
  public String getKey() {
    return key;
  }

  /**
   * The type of bean the query is fetching.
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * The call stack involved.
   */
  public CallOrigin getCallOrigin() {
    return callOrigin;
  }

  public String getTopElement() {
    return callOrigin.getTopElement();
  }

  @Override
  public String toString() {
    return "key[" + key + "] type[" + beanType + "] " + callOrigin.getTopElement();
  }

  @Override
  public int hashCode() {
    int hc = 92821 * callOrigin.hashCode();
    hc = 92821 * hc + beanType.hashCode();
    hc = 92821 * hc + queryHash;
    return hc;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ObjectGraphOrigin)) {
      return false;
    }

    ObjectGraphOrigin e = (ObjectGraphOrigin) obj;
    return e.queryHash == queryHash
      && e.beanType.equals(beanType)
      && e.callOrigin.equals(callOrigin);
  }
}
