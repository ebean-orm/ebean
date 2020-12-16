package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.Collection;

/**
 * Save details for a simple scalar element collection.
 */
class SaveManyElementCollection extends SaveManyBase {

  private Collection<?> collection;

  SaveManyElementCollection(DefaultPersister persister, boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(persister, insertedParent, many, parentBean, request);
  }

  private boolean modifiedCollection() {
    return collection != null && (insertedParent || BeanCollectionUtil.isModified(value));
  }

  @Override
  void save() {
    collection = BeanCollectionUtil.getActualEntries(value);
    if (modifiedCollection()) {
      preElementCollectionUpdate();
      if (insertedParent && request.isQueueSaveMany()) {
        request.addSaveMany(this);
      } else {
        saveCollection();
      }
    }
  }

  @Override
  public void saveBatch() {
    saveCollection();
  }

  private void saveCollection() {
    SpiSqlUpdate proto = many.insertElementCollection();
    Object parentId = request.getBeanId();
    for (Object value : collection) {
      final SpiSqlUpdate sqlInsert = proto.copy();
      sqlInsert.setParameter(parentId);
      many.bindElementValue(sqlInsert, value);
      persister.addToFlushQueue(sqlInsert, transaction, 2);
    }
    resetModifyState();
    postElementCollectionUpdate();
  }
}
