package com.avaje.ebean.event.changelog;

import com.avaje.ebean.event.BeanPersistRequest;

/**
 * Used to provide fine grained control over what persist requests are included in the change log.
 */
public interface ChangeLogFilter {

  /**
   * Return true if this insert request should be included in the change log.
   */
  boolean includeInsert(BeanPersistRequest<?> insertRequest);

  /**
   * Return true if this update request should be included in the change log.
   */
  boolean includeUpdate(BeanPersistRequest<?> updateRequest);

  /**
   * Return true if this delete request should be included in the change log.
   */
  boolean includeDelete(BeanPersistRequest<?> deleteRequest);

}
