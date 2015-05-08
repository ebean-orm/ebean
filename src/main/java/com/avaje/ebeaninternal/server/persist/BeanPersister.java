package com.avaje.ebeaninternal.server.persist;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Defines bean insert update and delete implementation.
 */
public interface BeanPersister {

	/**
	 * execute the insert bean request.
	 */
	public void insert(PersistRequestBean<?>  request) throws PersistenceException;

	/**
	 * execute the update bean request.
	 */
	public void update(PersistRequestBean<?>  request) throws PersistenceException;

	/**
	 * execute the delete bean request.
	 */
	public void delete(PersistRequestBean<?> request) throws PersistenceException;

}
