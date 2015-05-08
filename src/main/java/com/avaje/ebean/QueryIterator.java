package com.avaje.ebean;

import java.util.Iterator;

/**
 * Used to provide iteration over query results.
 * <p>
 * This can be used when you want to process a very large number of results and
 * means that you don't have to hold all the results in memory at once (unlike
 * findList(), findSet() etc where all the beans are held in the List or Set
 * etc).
 * </p>
 * 
 * <pre class="code">
 * 
 * Query&lt;Customer&gt; query = server.find(Customer.class)
 *     .fetch(&quot;contacts&quot;, new FetchConfig().query(2))
 *     .where().gt(&quot;id&quot;, 0)
 *     .orderBy(&quot;id&quot;)
 *     .setMaxRows(2);
 * 
 * QueryIterator&lt;Customer&gt; it = query.findIterate();
 * try {
 *   while (it.hasNext()) {
 *     Customer customer = it.next();
 *     // do something with customer...
 *   }
 * } finally {
 *   // close the associated resources
 *   it.close();
 * }
 * </pre>
 * 
 * @author rbygrave
 * 
 * @param <T>
 *          the type of entity bean in the iteration
 */
public interface QueryIterator<T> extends Iterator<T>, java.io.Closeable {

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   */
  boolean hasNext();

  /**
   * Returns the next element in the iteration.
   */
  T next();

  /**
   * Remove is not allowed.
   */
  void remove();

  /**
   * Close the underlying resources held by this iterator.
   */
  void close();
}
