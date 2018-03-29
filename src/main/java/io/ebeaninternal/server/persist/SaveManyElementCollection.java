package io.ebeaninternal.server.persist;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanCollectionUtil;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

import java.util.Collection;

/**
 * Save details for a simple scalar element collection.
 */
class SaveManyElementCollection extends SaveManyBase {

  SaveManyElementCollection(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(insertedParent, many, parentBean, request);
  }

  @Override
  void save() {

    Collection<?> collection = BeanCollectionUtil.getActualEntries(value);
    if (collection == null || !BeanCollectionUtil.isModified(value)) {
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

    for (Object value : collection) {

      sqlInsert.setNextParameter(parentId);
      many.bindElementValue(sqlInsert, value);
      server.execute(sqlInsert, transaction);
    }

    transaction.depth(-1);
    resetModifyState();
    postElementCollectionUpdate();
  }
}
