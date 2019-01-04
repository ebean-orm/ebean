package io.ebeaninternal.server.persist;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.Map;
import java.util.Set;

/**
 * Save details for a simple scalar map element collection.
 */
class SaveManyElementCollectionMap extends SaveManyBase {

  SaveManyElementCollectionMap(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(insertedParent, many, parentBean, request);
  }

  @SuppressWarnings("unchecked")
  @Override
  void save() {

    Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) BeanCollectionUtil.getActualEntries(value);
    if (entries == null || !BeanCollectionUtil.isModified(value)) {
      return;
    }

    Object parentId = request.getBeanId();
    SpiEbeanServer server = request.getServer();
    if (!insertedParent) {
      request.preElementCollectionUpdate();
      SqlUpdate sqlDelete = many.deleteByParentId(parentId, null);
      server.execute(sqlDelete, transaction);
    }

    transaction.depth(+1);

    String insert = many.insertElementCollection();
    SqlUpdate sqlInsert = server.createSqlUpdate(insert);

    for (Map.Entry<?, ?> entry : entries) {
      sqlInsert.setNextParameter(parentId);
      sqlInsert.setNextParameter(entry.getKey());
      many.bindElementValue(sqlInsert, entry.getValue());
      server.execute(sqlInsert, transaction);
    }

    transaction.depth(-1);
    resetModifyState();
    postElementCollectionUpdate();
  }
}
