package com.avaje.ebean;

import java.util.List;
import java.util.concurrent.Future;

/**
 * FutureIds represents the result of a background query execution for the Id's.
 * <p>
 * It extends the java.util.concurrent.Future with the ability to get the Id's
 * while the query is still executing in the background.
 * </p>
 * 
 * @author rbygrave
 */
public interface FutureIds<T> extends Future<List<Object>> {

  /**
   * Returns the original query used to fetch the Id's.
   */
  Query<T> getQuery();

  /**
   * Return the list of Id's which could be partially populated.
   * <p>
   * That is the query getting the id's could still be running and adding id's
   * to this list.
   * </p>
   * <p>
   * To get the list of Id's ensuring the query has finished use the
   * {@link Future#get()} method instead of this one.
   * </p>
   */
  List<Object> getPartialIds();
}
