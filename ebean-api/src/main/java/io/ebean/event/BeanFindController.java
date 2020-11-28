package io.ebean.event;

import io.ebean.bean.BeanCollection;

/**
 * Used to override the finding implementation for a bean.
 * <p>
 * For beans that are not in a JDBC data source you can implement this handle
 * bean finding. For example, read a log file building each entry as a bean and
 * returning that.
 * </p>
 * <p>
 * There are a number of internal BeanFinders in Ebean to return meta data from
 * Ebean at runtime such as query execution statistics etc.
 * </p>
 */
public interface BeanFindController {

  /**
   * Return true if this BeanPersistController should be registered for events
   * on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Return true if this controller should intercept and process this find request.
   * <p>
   * Return false to allow the default behavior to process the request.
   */
  boolean isInterceptFind(BeanQueryRequest<?> request);

  /**
   * Find a bean using its id or unique predicate.
   */
  <T> T find(BeanQueryRequest<T> request);

  /**
   * Return true if this controller should intercept and process this findMany request.
   * <p>
   * Return false to allow the default behavior to process the request.
   */
  boolean isInterceptFindMany(BeanQueryRequest<?> request);

  /**
   * Return a List, Set or Map for the given find request.
   * <p>
   * Note the returning object is cast to a List Set or Map so you do need to
   * get the return type right.
   * </p>
   */
  <T> BeanCollection<T> findMany(BeanQueryRequest<T> request);

}
