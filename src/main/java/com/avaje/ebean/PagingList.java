package com.avaje.ebean;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Used to page through a query result rather than fetching all the results in a
 * single query.
 * <p>
 * Has the ability to use background threads to 'fetch ahead' the next page and
 * get the total row count.
 * </p>
 * <p>
 * If you are building a stateless web application and not keeping the
 * PagingList over multiple requests then there is not much to be gained in
 * using PagingList. Instead you can just use {@link Query#setFirstRow(int)} and
 * {@link Query#setMaxRows(int)}.
 * </p>
 * 
 * <p>
 * If you are using PagingList is a stateful web application where the
 * PagingList is held over multiple requests then PagingList provides the extra
 * benefits of
 * <ul>
 * <li>Fetch ahead - automatically fetching the next page via background query
 * execution</li>
 * <li>Automatic propagation of the persistence context</li>
 * </ul>
 * </p>
 * <p>
 * So with PagingList when you use Page 2 it can automatically fetch Page 3 data
 * in the background (using a findFutureList() query). It also automatically
 * propagates the persistence context so that all the queries executed by the
 * PagingList all use the same persistence context.
 * </p>
 * 
 * <pre>
 * PagingList&lt;TOne&gt; pagingList =
 *     Ebean.find(TOne.class)
 *         .where().gt(&quot;name&quot;, &quot;2&quot;)
 *         .findPagingList(10);
 * 
 * // get the row count in the background...
 * // ... otherwise it is fetched on demand
 * // ... when getRowCount() or getPageCount()
 * // ... is called
 * pagingList.getFutureRowCount();
 * 
 * // get the first page
 * Page&lt;TOne&gt; page = pagingList.getPage(0);
 * 
 * // get the beans from the page as a list
 * List&lt;TOne&gt; list = page.getList();
 * </pre>
 * 
 * @author rbygrave
 * 
 * @param <T>
 *          the entity bean type
 */
public interface PagingList<T> {

  /**
   * Refresh will clear all the pages and row count forcing them to be
   * re-fetched when next required.
   */
  public void refresh();

  // public void fetchAll();
  // public String? getOrderBy();
  // public void setOrderBy(String?);

  /**
   * By default fetchAhead is true so use this to turn off fetchAhead.
   * <p>
   * Set this to false if you don't want to fetch ahead using background
   * fetching.
   * <p>
   * If set to true (or left as to default) then the next page is fetched in the
   * background as soon as the list is accessed.
   * </p>
   */
  public PagingList<T> setFetchAhead(boolean fetchAhead);

  /**
   * Return the Future for getting the total row count.
   */
  public Future<Integer> getFutureRowCount();

  /**
   * Return the data for all the pages in the form of a single List.
   * <p>
   * Iterating through this list will automatically fire the paging queries as
   * required.
   * </p>
   */
  public List<T> getAsList();

  /**
   * Return the page size. This is the number of rows per page.
   */
  public int getPageSize();

  /**
   * Return the total row count.
   * <p>
   * This gets the result from getFutureRowCount and will wait until that query
   * has completed.
   * </p>
   */
  public int getTotalRowCount();

  /**
   * Return the total page count.
   * <p>
   * This is based on the total row count. This will wait until the row count
   * has returned if it has not already.
   * </p>
   */
  public int getTotalPageCount();

  /**
   * Return the page for a given page position (starting at 0).
   */
  public Page<T> getPage(int i);

}
