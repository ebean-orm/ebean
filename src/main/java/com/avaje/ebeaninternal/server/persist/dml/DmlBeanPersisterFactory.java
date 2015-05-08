package com.avaje.ebeaninternal.server.persist.dml;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.persist.BeanPersister;
import com.avaje.ebeaninternal.server.persist.BeanPersisterFactory;

/**
 * Factory for creating a DmlBeanPersister for a bean type.
 */
public class DmlBeanPersisterFactory implements BeanPersisterFactory {
	
	private final MetaFactory metaFactory;
	
	public DmlBeanPersisterFactory(DatabasePlatform dbPlatform) {
		this.metaFactory = new MetaFactory(dbPlatform);
	}
	
	
	/**
	 * Create a DmlBeanPersister for the given bean type.
	 */
	public BeanPersister create(BeanDescriptor<?> desc) {
		
		UpdateMeta updMeta = metaFactory.createUpdate(desc);
		DeleteMeta delMeta = metaFactory.createDelete(desc);
		InsertMeta insMeta = metaFactory.createInsert(desc);
		
		return new DmlBeanPersister(updMeta, insMeta, delMeta);
		
	}
	
}
