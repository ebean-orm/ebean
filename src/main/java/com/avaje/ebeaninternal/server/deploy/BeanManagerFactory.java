package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.persist.BeanPersisterFactory;
import com.avaje.ebeaninternal.server.persist.dml.DmlBeanPersisterFactory;

/**
 * Creates BeanManagers.
 */
public class BeanManagerFactory {

  final BeanPersisterFactory persisterFactory;

  public BeanManagerFactory(DatabasePlatform dbPlatform) {
    persisterFactory = new DmlBeanPersisterFactory(dbPlatform);
  }

  public <T> BeanManager<T> create(BeanDescriptor<T> desc) {

    return new BeanManager<>(desc, persisterFactory.create(desc));
  }

}
