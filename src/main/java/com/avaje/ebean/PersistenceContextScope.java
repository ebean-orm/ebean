package com.avaje.ebean;

/**
 * Defines the scope for PersistenceContext.
 * <p/>
 * Ebean has traditionally used Transaction scope for the PersistenceContext. This is used to change the scope to
 * use (by default) and explicitly set the scope to use for an individual query.
 *
 * @see com.avaje.ebean.config.ServerConfig#setPersistenceContextScope(PersistenceContextScope)
 * @see Query#setPersistenceContextScope(PersistenceContextScope)
 */
public enum PersistenceContextScope {

  /**
   * PersistenceContext is scoped to the transaction.
   * <p/>
   * If a transaction spans 2 or more queries that fetch the same bean in terms of same type
   * and same Id value then they share the same bean instance.
   * <p/>
   * You may want to change to use QUERY scope when you want a query executing in a transaction to effectively
   * ignore beans that have already been loaded (by other queries in the same transaction) and instead get a
   * 'fresh copy' of the bean.
   */
  TRANSACTION,

  /**
   * PersistenceContext is scoped to the query.
   * <p/>
   * This means that for this query running in an existing transaction then it will effectively ignore any beans
   * that have already been queried/loaded by prior queries in the same transaction.
   * <p/>
   * You may use QUERY scope on a query that is executed in a transaction and you want to get a 'fresh copy' of the bean.
   */
  QUERY
}
