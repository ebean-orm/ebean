package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Factory for creating BeanPersister implementations.
 */
public interface BeanPersisterFactory {
	
	/**
	 * Create the BeanPersister implemenation for a given type.
	 */
	public BeanPersister create(BeanDescriptor<?> desc);

}
