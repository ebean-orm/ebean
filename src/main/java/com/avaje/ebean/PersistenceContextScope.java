package com.avaje.ebean;

/**
 * Defines the scope for PersistenceContext.
 * <p/>
 * Ebean has traditionally used Transaction scope for the PersistenceContext. This is used to change the scope to
 * use (by default) and explicitly set the scope to use for an individual query.
 *
 * @see com.avaje.ebean.config.ServerConfig#setPersistenceContextScope(PersistenceContextScope)
 * @see com.avaje.ebean.Query#setPersistenceContextScope(PersistenceContextScope)
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
  QUERY,

  /**
   * EXPERIMENTAL FEATURE - This is not expected to be used and somewhat experimental.
   * This NONE option effectively means that a PersistenceContext is not used when building the object graph and
   * subsequent lazy loading.
   * <p/>
   * You should ONLY use this when treating the resulting object graph as read only and even then you would be best
   * to use QUERY (or TRANSACTION).
   * <p/>
   * A query executed with NONE can build a object graph where there are multiple instances that represent the same
   * 'logical bean' by type and Id value (multiple instances of 'Customer 42'). Getting multiple instances that
   * represent the same logical bean (same row in the database) means that it is potentially dangerous/confusing to
   * use this scope when modifying the beans as multiple instances represent the same underlying rows in the database.
   * <p/>
   * Generally you would expect to always use TRANSACTION or QUERY scope.
   */
  NONE
}
