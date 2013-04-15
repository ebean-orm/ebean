package com.avaje.ebean;

import java.util.List;

/**
 * Represents a Page of results that is part of a PagingList.
 * <p>
 * Typically a Page represents the data that is shown to the user at a single
 * time - and the user 'pages' through a large list.
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <T>
 *          the entity bean type
 * 
 * @see Query#findPagingList(int)
 * @see PagingList
 */
public interface Page<T> {

  /**
   * Return the list of entities for this page.
   */
  public List<T> getList();

  /**
   * Return the total row count for all pages.
   */
  public int getTotalRowCount();

  /**
   * Return the total number of pages.
   */
  public int getTotalPageCount();

  /**
   * Return the index position of this page.
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
   * Return the next page.
   */
  public Page<T> next();

  /**
   * Return the previous page.
   */
  public Page<T> prev();

  /**
   * Helper method to return a "X to Y of Z" string for this page where X is the
   * first row, Y the last row and Z the total row count.
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
