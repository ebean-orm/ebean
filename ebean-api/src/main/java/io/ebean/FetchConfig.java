package io.ebean;

import java.io.Serializable;

/**
 * Defines how a relationship is fetched via either normal SQL join,
 * a eager secondary query, via lazy loading or via eagerly hitting L2 cache.
 * <p>
 * <pre>{@code
 * // Normal fetch join results in a single SQL query
 * List<Order> list = DB.find(Order.class).fetch("details").findList();
 *
 * }</pre>
 * <p>
 * Example: Using a "query join" instead of a "fetch join" we instead use 2 SQL queries
 *
 * <pre>{@code
 *
 * // This will use 2 SQL queries to build this object graph
 * List<Order> list =
 *     DB.find(Order.class)
 *         .fetch("details", FetchConfig.ofQuery())
 *         .findList();
 *
 * // query 1) find order
 * // query 2) find orderDetails where order.id in (?,?...) // first 100 order id's
 *
 * }</pre>
 */
public final class FetchConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final int JOIN_MODE = 0;
  private static final int QUERY_MODE = 1;
  private static final int LAZY_MODE = 2;
  private static final int CACHE_MODE = 3;

  private final int mode;
  private final int batchSize;
  private final int hashCode;

  private FetchConfig(int mode, int batchSize) {
    this.mode = mode;
    this.batchSize = batchSize;
    this.hashCode = mode + 10 * batchSize;
  }

  /**
   * Return FetchConfig to eagerly fetch the relationship using L2 cache.
   * <p>
   * Any cache misses will be loaded by secondary query to the database.
   */
  public static FetchConfig ofCache() {
    return new FetchConfig(CACHE_MODE, 100);
  }

  /**
   * Return FetchConfig to eagerly fetch the relationship using a secondary query.
   */
  public static FetchConfig ofQuery() {
    return new FetchConfig(QUERY_MODE, 100);
  }

  /**
   * Return FetchConfig to eagerly fetch the relationship using a secondary with a given batch size.
   */
  public static FetchConfig ofQuery(int batchSize) {
    return new FetchConfig(QUERY_MODE, batchSize);
  }

  /**
   * Return FetchConfig to lazily load the relationship.
   */
  public static FetchConfig ofLazy() {
    return new FetchConfig(LAZY_MODE, 0);
  }

  /**
   * Return FetchConfig to lazily load the relationship specifying the batch size.
   */
  public static FetchConfig ofLazy(int batchSize) {
    return new FetchConfig(LAZY_MODE, batchSize);
  }

  /**
   * Return FetchConfig to fetch the relationship using SQL join.
   */
  public static FetchConfig ofDefault() {
    return new FetchConfig(JOIN_MODE, 100);
  }

  /**
   * Return the batch size for fetching.
   */
  public int getBatchSize() {
    return batchSize;
  }

  /**
   * Return true if the fetch should use the L2 cache.
   */
  public boolean isCache() {
    return mode == CACHE_MODE;
  }

  /**
   * Return true if the fetch should be a eager secondary query.
   */
  public boolean isQuery() {
    return mode == QUERY_MODE;
  }

  /**
   * Return true if the fetch should be a lazy query.
   */
  public boolean isLazy() {
    return mode == LAZY_MODE;
  }

  /**
   * Return true if the fetch should try to use SQL join.
   */
  public boolean isJoin() {
    return mode == JOIN_MODE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return (hashCode == ((FetchConfig) o).hashCode);
  }

  @Override
  public int hashCode() {
    return hashCode;
  }
}
