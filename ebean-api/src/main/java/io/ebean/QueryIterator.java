package io.ebean;

import java.util.Iterator;

/**
 * Used to provide iteration over query results.
 * <p>
 * This can be used when you want to process a very large number of results and
 * means that you don't have to hold all the results in memory at once (unlike
 * findList(), findSet() etc where all the beans are held in the List or Set
 * etc).
 * </p>
 * <p>
 * Note that findIterate (and findEach and findEachWhile) uses a "per graph"
 * persistence context scope and adjusts jdbc fetch buffer size for large
 * queries. As such it is better to use findList for small queries.
 * </p>
 * <p>
 * Remember that with {@link QueryIterator} you must call {@link QueryIterator#close()}
 * when you have finished iterating the results. Use "try with resources" or ensure it
 * is closed in a finally block.
 * </p>
 * <h3>Try finally style</h3>
 * <pre>{@code
 *
 *  Query<Customer> query = database.find(Customer.class)
 *     .where().gt("id", 0)
 *     .orderBy("id")
 *     .setMaxRows(2);
 *
 *  QueryIterator<Customer> it = query.findIterate();
 *  try {
 *    while (it.hasNext()) {
 *      Customer customer = it.next();
 *      // do something with customer ...
 *    }
 *  } finally {
 *    // close the underlying resources
 *    it.close();
 *  }
 *
 * }</pre>
 * <p>
 * <h3>Try with resources style</h3>
 * <pre>{@code
 *
 *  // try with resources
 *  try (QueryIterator<Customer> it = query.findIterate()) {
 *    while (it.hasNext()) {
 *      Customer customer = it.next();
 *      // do something with customer ...
 *    }
 *  }
 *
 * }</pre>
 *
 * @param <T> the type of entity bean in the iteration
 */
public interface QueryIterator<T> extends Iterator<T>, AutoCloseable {

  /**
   * Returns <tt>true</tt> if the iteration has more elements.
   */
  @Override
  boolean hasNext();

  /**
   * Returns the next element in the iteration.
   */
  @Override
  T next();

  /**
   * Close the underlying resources held by this iterator.
   */
  @Override
  void close();
}
