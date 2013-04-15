package com.avaje.ebean.bean;

import java.io.Serializable;

/**
 * Represents a "origin" of an ORM object graph. This combines the call stack
 * and query plan hash.
 * <p>
 * The call stack is included so that the query can have different tuned fetches
 * for each unique call stack. For example, a query to fetch a customer could be
 * called by three different methods and each can be treated as a separate
 * origin point (and autoFetch can tune each one separately).
 * </p>
 */
public final class ObjectGraphOrigin implements Serializable {

  private static final long serialVersionUID = 410937765287968707L;

  private final CallStack callStack;

  private final String key;

  private final String beanType;

  public ObjectGraphOrigin(int queryHash, CallStack callStack, String beanType) {
    this.callStack = callStack;
    this.beanType = beanType;
    this.key = callStack.getOriginKey(queryHash);
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
  public CallStack getCallStack() {
    return callStack;
  }

  public String getFirstStackElement() {
    return callStack.getFirstStackTraceElement().toString();
  }

  public String toString() {
    return key + " " + beanType + " " + callStack.getFirstStackTraceElement();
  }

}
