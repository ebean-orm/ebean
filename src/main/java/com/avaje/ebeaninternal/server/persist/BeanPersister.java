package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

import javax.persistence.PersistenceException;

/**
 * Defines bean insert update and delete implementation.
 */
public interface BeanPersister {

  /**
   * execute the insert bean request.
   */
  void insert(PersistRequestBean<?> request) throws PersistenceException;

  /**
   * execute the update bean request.
   */
  void update(PersistRequestBean<?> request) throws PersistenceException;

  /**
   * execute the delete bean request.
   */
  int delete(PersistRequestBean<?> request) throws PersistenceException;

}
