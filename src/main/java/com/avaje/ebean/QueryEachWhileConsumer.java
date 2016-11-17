package com.avaje.ebean;

/**
 * Used to process a query result one bean at a time via a callback to this
 * visitor.
 * <p>
 * If you wish to stop further processing return false from the accept method.
 * </p>
 * <p>
 * Unlike findList() and findSet() using a QueryResultVisitor does not require
 * all the beans in the query result to be held in memory at once. This makes
 * QueryResultVisitor useful for processing large queries.
 * </p>
 * <p/>
 * <pre>{@code
 *
 * Query<Customer> query = server.find(Customer.class)
 *     .fetchQuery("contacts")
 *     .where().gt("id", 0)
 *     .orderBy("id")
 *     .setMaxRows(2);
 *
 * query.findEachWhile((Customer customer) -> {
 *
 *     // do something with customer
 *     System.out.println("-- visit " + customer);
 *
 *     // return true to continue processing or false to stop
 *     return (customer.getId() < 40);
 * });
 *
 * }</pre>
 *
 * @param <T> the type of entity bean being queried.
 */
public interface QueryEachWhileConsumer<T> {

  /**
   * Process the bean and return true if you want to continue processing more
   * beans. Return false if you want to stop processing further.
   *
   * @param bean the entity bean to process
   * @return true to continue processing more beans or false to stop.
   */
  boolean accept(T bean);
}
