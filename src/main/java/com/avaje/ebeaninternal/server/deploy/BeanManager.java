package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.persist.BeanPersister;

/**
 * Holds the BeanDescriptor and its associated BeanPersister.
 */
public class BeanManager<T> {

	private final BeanPersister persister;

	private final BeanDescriptor<T> descriptor;

	public BeanManager(BeanDescriptor<T> descriptor, BeanPersister persister) {
		this.descriptor = descriptor;
		this.persister = persister;
	}

	/**
	 * Return the associated BeanPersister.
	 */
	public BeanPersister getBeanPersister() {
		return persister;
	}

	/**
	 * Return the BeanDescriptor.
	 */
	public BeanDescriptor<T> getBeanDescriptor() {
		return descriptor;
	}

}
