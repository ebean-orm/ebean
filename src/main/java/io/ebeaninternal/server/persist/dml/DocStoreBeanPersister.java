package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BeanPersister;

import javax.persistence.PersistenceException;

/**
 * Document store based BeanPersister.
 */
class DocStoreBeanPersister implements BeanPersister {

  private final GeneratedProperties generatedProperties;

  DocStoreBeanPersister(GeneratedProperties generatedProperties) {
    this.generatedProperties = generatedProperties;
  }

  @Override
  public void insert(PersistRequestBean<?> request) throws PersistenceException {
    //request.setIdValueForDocStore();
    generatedProperties.preInsert(request.getEntityBean(), request.now());
    request.docStorePersist();
  }

  @Override
  public void update(PersistRequestBean<?> request) throws PersistenceException {
    generatedProperties.preUpdate(request.getEntityBean(), request.now());
    request.docStorePersist();
  }

  @Override
  public int delete(PersistRequestBean<?> request) throws PersistenceException {
    request.docStorePersist();
    return 0;
  }
}
