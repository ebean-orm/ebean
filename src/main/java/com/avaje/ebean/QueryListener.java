package com.avaje.ebean;

/**
 * Provides a mechanism for processing a query one bean at a time.
 * <p>
 * This is useful when the query will return a large number of results and you
 * want to process the beans one at a time rather than whole all of the beans in
 * memory at once.
 * </p>
 * 
 * <pre class="code">
 * QueryListener&lt;Order&gt; listener = ...;
 *    
 * Query&lt;Order&gt; query  = Ebean.createQuery(Order.class);
 *    
 * // set the listener that will process each order one at a time
 * query.setListener(listener);
 *    
 * // execute the query. Note that the returned
 * // list will be empty ... so don't bother assigning it
 * query.findList();
 * </pre>
 * 
 * @param <T>
 *          the type of entity bean
 */
public interface QueryListener<T> {

  /**
   * Process the bean that has just been read.
   * <p>
   * This bean will not be added to the List Set or Map and nor will it be put
   * into the PersistenceContext. This is what makes this a good way to process
   * a large result set (which could normally use a lot of memory).
   * </p>
   */
  public void process(T bean);
}
