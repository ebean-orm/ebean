package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.persist.BeanPersister;
import com.avaje.ebeaninternal.server.persist.BeanPersisterFactory;
import com.avaje.ebeaninternal.server.persist.dml.DmlBeanPersisterFactory;

/**
 * Creates BeanManagers.
 */
public class BeanManagerFactory {

	final BeanPersisterFactory peristerFactory;
	
	public BeanManagerFactory(ServerConfig config, DatabasePlatform dbPlatform) {
		peristerFactory = new DmlBeanPersisterFactory(dbPlatform);
	}
	
	public <T> BeanManager<T> create(BeanDescriptor<T> desc) {
		
		BeanPersister persister = peristerFactory.create(desc);

		return new BeanManager<T>(desc, persister);
	}

}
