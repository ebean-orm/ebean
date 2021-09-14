package io.ebeaninternal.server.persist.dml;

import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BeanPersister;

import javax.persistence.PersistenceException;

/**
 * Document store based BeanPersister.
 */
final class DocStoreBeanPersister implements BeanPersister {

  private final GeneratedProperties generatedProperties;

  DocStoreBeanPersister(GeneratedProperties generatedProperties) {
    this.generatedProperties = generatedProperties;
  }

  @Override
  public void insert(PersistRequestBean<?> request) throws PersistenceException {
    //request.setIdValueForDocStore();
    generatedProperties.preInsert(request.entityBean(), request.now());
    request.docStorePersist();
  }

  @Override
  public void update(PersistRequestBean<?> request) throws PersistenceException {
    generatedProperties.preUpdate(request.entityBean(), request.now());
    request.docStorePersist();
  }

  @Override
  public int delete(PersistRequestBean<?> request) throws PersistenceException {
    request.docStorePersist();
    return 0;
  }
}
