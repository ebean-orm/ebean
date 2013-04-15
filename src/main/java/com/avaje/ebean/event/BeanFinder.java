package com.avaje.ebean.event;

import com.avaje.ebean.bean.BeanCollection;

/**
 * Used to override the finding implementation for a bean.
 * <p>
 * For beans that are not in a JDBC data source you can implement this handle
 * bean finding. For example, read a log file building each entry as a bean and
 * returning that.
 * </p>
 * <p>
 * There are a number of internal BeanFinders in Ebean to return meta data from
 * Ebean at runtime such as query execution statistics etc. See the beans in
 * com.avaje.ebean.meta and finders in com.avaje.ebean.server.meta.
 * </p>
 */
public interface BeanFinder<T> {

  /**
   * Find a bean using its id or unique predicate.
   */
  public T find(BeanQueryRequest<T> request);

  /**
   * Return a List, Set or Map for the given find request.
   * <p>
   * Note the returning object is cast to a List Set or Map so you do need to
   * get the return type right.
   * </p>
   */
  public BeanCollection<T> findMany(BeanQueryRequest<T> request);

}
