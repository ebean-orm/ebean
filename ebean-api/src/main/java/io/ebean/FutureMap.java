package io.ebean;

import jakarta.persistence.PersistenceException;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * FutureMap represents the result of a background query execution that will
 * return a map of entities.
 * <p>
 * It extends the java.util.concurrent.Future with the ability to cancel the
 * query, check if it is finished and get the resulting list waiting for the
 * query to finish (ie. the standard features of java.util.concurrent.Future).
 * </p>
 * <p>
 * A simple example:
 * </p>
 * <pre>{@code
 *
 *  // create a query to find all orders
 * Query<Long,Order> query = DB.find(Order.class)
 * .setMapKey("id");
 *
 *  // execute the query in a background thread
 *  // immediately returning the futureMap
 * FutureMap<Long,Order> futureMap = query.findFutureMap();
 *
 *  // do something else ...
 *
 * if (!futureMap.isDone()){
 * 	// we can cancel the query execution. This will cancel
 * // the underlying query if that is supported by the JDBC
 * // driver and database
 * 	futureMap.cancel(true);
 * }
 *
 * if (!futureMap.isCancelled()){
 * 	// wait for the query to finish and return the map
 * 	Map<Long,Order> map = futureMap.get();
 * 	...
 * }
 *
 * }</pre>
 */
public interface FutureMap<K, T> extends Future<Map<K, T>> {

  /**
   * Return the query that is being executed by a background thread.
   */
  Query<T> getQuery();

  /**
   * Same as {@link #get()} but wraps InterruptedException and ExecutionException in the
   * unchecked PersistenceException.
   *
   * @return The query list result
   * @throws PersistenceException when a InterruptedException or ExecutionException occurs.
   */
  Map<K, T> getUnchecked();

  /**
   * Same as {@link #get(long, TimeUnit)} but wraps InterruptedException
   * and ExecutionException in the unchecked PersistenceException.
   *
   * @return The query list result
   * @throws TimeoutException     if the wait timed out
   * @throws PersistenceException if a InterruptedException or ExecutionException occurs.
   */
  Map<K, T> getUnchecked(long timeout, TimeUnit unit) throws TimeoutException;

}
