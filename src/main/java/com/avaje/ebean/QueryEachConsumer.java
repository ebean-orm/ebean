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
 *
 * <pre class="code">
 *
 * Query&lt;Customer&gt; query = server.find(Customer.class)
 *     .where().gt(&quot;id&quot;, 0)
 *     .orderBy(&quot;id&quot;)
 *     .setMaxRows(2);
 *
 * query.findVisit((Customer customer) -> {
 *
 *     // do something with customer
 *     System.out.println(&quot;-- visit &quot; + customer);
 * });
 * </pre>
 * 
 * @param <T>
 *          the type of entity bean being queried.
 */
public interface QueryEachConsumer<T> {

  /**
   * Process the bean.
   * 
   * @param bean
   *          the entity bean to process
   */
  public void accept(T bean);
}
