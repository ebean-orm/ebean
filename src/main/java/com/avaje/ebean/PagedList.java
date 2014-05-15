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
 * @param <T>
 *          the entity bean type
 * 
 * @see Query#findPagedList(int, int)
 */
public interface PagedList<T> {

  /**
   * Initiate the loading of the total row count in the background.
   */
  public void loadRowCount();

  /**
   * Return the Future row count. You might get this if you wish to cancel the total row count query
   * or specify a timeout for that query.
   */
  public Future<Integer> getFutureRowCount();

  /**
   * Return the list of entities for this page.
   */
  public List<T> getList();

  /**
   * Return the total row count for all pages.
   */
  public int getTotalRowCount();

  /**
   * Return the total number of pages based on the page size and total row count.
   */
  public int getTotalPageCount();

  /**
   * Return the index position of this page. Zero based.
   */
  public int getPageIndex();

  /**
   * Return true if there is a next page.
   */
  public boolean hasNext();

  /**
   * Return true if there is a previous page.
   */
  public boolean hasPrev();

  /**
   * Helper method to return a "X to Y of Z" string for this page where X is the first row, Y the
   * last row and Z the total row count.
   * 
   * @param to
   *          String to put between the first and last row
   * @param of
   *          String to put between the last row and the total row count
   * 
   * @return String of the format XtoYofZ.
   */
  public String getDisplayXtoYofZ(String to, String of);
}
