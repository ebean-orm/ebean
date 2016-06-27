package com.avaje.ebean;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * FutureList represents the result of a background query execution that will
 * return a list of entities.
 * <p>
 * It extends the java.util.concurrent.Future with the ability to cancel the
 * query, check if it is finished and get the resulting list waiting for the
 * query to finish (ie. the standard features of java.util.concurrent.Future).
 * </p>
 * <p>
 * A simple example:
 * </p>
 * 
 * <pre>{@code
 *  // create a query to find all orders
 * Query<Order> query = Ebean.find(Order.class);
 * 
 *  // execute the query in a background thread
 *  // immediately returning the futureList
 * FutureList<Order> futureList = query.findFutureList();
 * 
 *  // do something else ... 
 * 
 * if (!futureList.isDone()){
 * 	// we can cancel the query execution. This will cancel
 * // the underlying query if that is supported by the JDBC
 * // driver and database
 * 	futureList.cancel(true);
 * }
 * 
 * 
 * if (!futureList.isCancelled()){
 * 	// wait for the query to finish and return the list
 * 	List<Order> list = futureList.get();
 * 	...
 * }
 * 
 * }</pre>
 */
public interface FutureList<T> extends Future<List<T>> {

  /**
   * Return the query that is being executed by a background thread.
   */
  Query<T> getQuery();

  /**
   * Same as {@link #get()} but wraps InterruptedException and ExecutionException in the
   * unchecked PersistenceException.
   *
   * @return The query list result
   *
   * @throws PersistenceException when a InterruptedException or ExecutionException occurs.
   */
  List<T> getUnchecked();

  /**
   * Same as {@link #get(long, TimeUnit)} but wraps InterruptedException
   * and ExecutionException in the unchecked PersistenceException.
   *
   * @return The query list result
   *
   * @throws TimeoutException if the wait timed out
   * @throws PersistenceException if a InterruptedException or ExecutionException occurs.
   */
  List<T> getUnchecked(long timeout, TimeUnit unit) throws TimeoutException;

}
