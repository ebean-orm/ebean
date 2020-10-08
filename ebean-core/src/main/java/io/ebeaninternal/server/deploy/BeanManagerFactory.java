package io.ebeaninternal.server.deploy;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.server.persist.BeanPersisterFactory;
import io.ebeaninternal.server.persist.dml.DmlBeanPersisterFactory;

/**
 * Creates BeanManagers.
 */
class BeanManagerFactory {

  private final BeanPersisterFactory persisterFactory;

  BeanManagerFactory(DatabasePlatform dbPlatform) {
    persisterFactory = new DmlBeanPersisterFactory(dbPlatform);
  }

  public <T> BeanManager<T> create(BeanDescriptor<T> desc) {
    return new BeanManager<>(desc, persisterFactory.create(desc));
  }

}
