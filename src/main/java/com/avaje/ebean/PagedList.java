package com.avaje.ebean;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Represents a page of results.
 * <p>
 * The benefit of using PagedList over just using the normal Query with
 * {@link Query#setFirstRow(int)} and {@link Query#setMaxRows(int)} is that it additionally wraps
 * functionality that can call {@link Query#findFutureRowCount()} to determine total row count,
 * total page count etc.
 * </p>
 * <p>
 * Internally this works using {@link Query#setFirstRow(int)} and {@link Query#setMaxRows(int)} on
 * the query. This translates into SQL that uses limit offset, rownum or row_number function to
 * limit the result set.
 * </p>
 *
 * <h4>Example: typical use including total row count</h4>
 * <pre>{@code
 *
 *     // We want to find the first 100 new orders
 *     //  ... 0 means first page
 *     //  ... page size is 100
 *
 *     PagedList<Order> pagedList
 *       = ebeanServer.find(Order.class)
 *       .where().eq("status", Order.Status.NEW)
 *       .order().asc("id")
 *       .findPagedList(0, 100);
 *
 *     // Optional: initiate the loading of the total
 *     // row count in a background thread
 *     pagedList.loadRowCount();
 *
 *     // fetch and return the list in the foreground thread
 *     List<Order> orders = pagedList.getList();
 *
 *     // get the total row count (from the future)
 *     int totalRowCount = pagedList.getTotalRowCount();
 *
 * }</pre>
 *
 * <h4>Example: No total row count required</h4>
 * <pre>{@code
 *
 *     // If you are not getting the 'first page' often
 *     // you do not bother getting the total row count again
 *     // so instead just get the page list of data
 *
 *     // fetch and return the list in the foreground thread
 *     List<Order> orders = pagedList.getList();
 *
 * }</pre>
 *
 * @param <T>
 *          the entity bean type
 * 
 * @see Query#findPagedList(int, int)
 */
public interface PagedList<T> {

  /**
   * Initiate the loading of the total row count in the background.
   * <pre>{@code
   *
   *     // initiate the loading of the total row count
   *     // in a background thread
   *     pagedList.loadRowCount();
   *
   *     // fetch and return the list in the foreground thread
   *     List<Order> orders = pagedList.getList();
   *
   *     // get the total row count (from the future)
   *     int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   *
   * <p>
   * Also note that using loadRowCount() and getTotalRowCount() rather than getFutureRowCount()
   * means that exceptions ExecutionException, InterruptedException, TimeoutException are instead
   * wrapped in the unchecked PersistenceException (which might be preferrable).
   * </p>
   */
  void loadRowCount();

  /**
   * Return the Future row count. You might get this if you wish to cancel the total row count query
   * or specify a timeout for the row count query.
   * <p>
   * The loadRowCount() & getTotalRowCount() methods internally make use of this getFutureRowCount() method.
   * Generally I expect people to prefer loadRowCount() & getTotalRowCount() over getFutureRowCount().
   * </p>
   * <pre>{@code
   *
   *     // initiate the row count query in the background thread
   *     Future<Integer> rowCount = pagedList.getFutureRowCount();
   *
   *     // fetch and return the list in the foreground thread
   *     List<Order> orders = pagedList.getList();
   *
   *     // now get the total count with a timeout
   *     Integer totalRowCount = rowCount.get(30, TimeUnit.SECONDS);
   *
   *     // or ge the total count without a timeout
   *     Integer totalRowCountViaFuture = rowCount.get();
   *
   *     // which is actually the same as ...
   *     int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   */
  Future<Integer> getFutureRowCount();

  /**
   * Return the list of entities for this page.
   */
  List<T> getList();

  /**
   * Return the total row count for all pages.
   * <p>
   * If loadRowCount() has already been called then the row count query is already executing in a background thread
   * and this gets the associated Future and gets the value waiting for the future to finish.
   * </p>
   * <p>
   * If loadRowCount() has not been called then this executes the find row count query and returns the result and this
   * will just occur in the current thread and not use a background thread.
   * </p>
   * <pre>{@code
   *
   *     // Optional: initiate the loading of the total
   *     // row count in a background thread
   *     pagedList.loadRowCount();
   *
   *     // fetch and return the list in the foreground thread
   *     List<Order> orders = pagedList.getList();
   *
   *     // get the total row count (which was being executed
   *     // in a background thread if loadRowCount() was used)
   *     int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   */
  int getTotalRowCount();

  /**
   * Return the total number of pages based on the page size and total row count.
   * <p>
   * This method requires that the total row count has been fetched and will invoke
   * the total row count query if it has not already been invoked.
   * </p>
   */
  int getTotalPageCount();

  /**
   * Return the index position of this page. Zero based.
   */
  int getPageIndex();

  /**
   * Return the page size used for this query.
   */
  int getPageSize();

  /**
   * Return true if there is a next page.
   * <p>
   * This method requires that the total row count has been fetched and will invoke
   * the total row count query if it has not already been invoked.
   * </p>
   */
  boolean hasNext();

  /**
   * Return true if there is a previous page.
   */
  boolean hasPrev();

  /**
   * Helper method to return a "X to Y of Z" string for this page where X is the first row, Y the
   * last row and Z the total row count.
   * <p>
   * This method requires that the total row count has been fetched and will invoke
   * the total row count query if it has not already been invoked.
   * </p>
   *
   * @param to
   *          String to put between the first and last row
   * @param of
   *          String to put between the last row and the total row count
   * 
   * @return String of the format XtoYofZ.
   */
  String getDisplayXtoYofZ(String to, String of);
}
