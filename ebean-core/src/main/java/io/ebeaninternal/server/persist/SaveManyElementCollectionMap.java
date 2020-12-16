package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiSqlUpdate;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.Map;
import java.util.Set;

/**
 * Save details for a simple scalar map element collection.
 */
class SaveManyElementCollectionMap extends SaveManyBase {

  private Set<Map.Entry<?, ?>> entries;

  SaveManyElementCollectionMap(DefaultPersister persister, boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(persister, insertedParent, many, parentBean, request);
  }

  private boolean modifiedCollection() {
    return entries != null && (insertedParent || BeanCollectionUtil.isModified(value));
  }

  @SuppressWarnings("unchecked")
  @Override
  void save() {
    entries = (Set<Map.Entry<?, ?>>) BeanCollectionUtil.getActualEntries(value);
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
    for (Map.Entry<?, ?> entry : entries) {
      final SpiSqlUpdate sqlInsert = proto.copy();
      sqlInsert.setParameter(parentId);
      sqlInsert.setParameter(entry.getKey());
      many.bindElementValue(sqlInsert, entry.getValue());
      persister.addToFlushQueue(sqlInsert, transaction, 2);
    }
    resetModifyState();
    postElementCollectionUpdate();
  }
}
