package io.ebeaninternal.server.persist.dml;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.persist.BeanPersister;
import io.ebeaninternal.server.persist.BeanPersisterFactory;

/**
 * Factory for creating a DmlBeanPersister for a bean type.
 */
public class DmlBeanPersisterFactory implements BeanPersisterFactory {

  private final DatabasePlatform dbPlatform;

  private final MetaFactory metaFactory;

  public DmlBeanPersisterFactory(DatabasePlatform dbPlatform) {
    this.dbPlatform = dbPlatform;
    this.metaFactory = new MetaFactory(dbPlatform);
  }

  /**
   * Create a DmlBeanPersister for the given bean type.
   */
  @Override
  public BeanPersister create(BeanDescriptor<?> desc) {

    if (desc.isDocStoreOnly()) {
      return new DocStoreBeanPersister(GeneratedProperties.of(desc));
    }

    UpdateMeta updMeta = metaFactory.createUpdate(desc);
    DeleteMeta delMeta = metaFactory.createDelete(desc);
    InsertMeta insMeta = metaFactory.createInsert(desc);
    return new DmlBeanPersister(dbPlatform, updMeta, insMeta, delMeta);
  }

}
