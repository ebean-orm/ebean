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
class SaveManySimpleMap extends SaveManyBase {

  SaveManySimpleMap(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(insertedParent, many, parentBean, request);
  }

  @SuppressWarnings("unchecked")
  @Override
  void save() {

    Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>)BeanCollectionUtil.getActualEntries(value);
    if (entries == null) {
      return;
    }

    Object parentId = request.getBeanId();

    SqlUpdate sqlDelete = many.deleteByParentId(parentId, null);

    SpiEbeanServer server = request.getServer();
    server.execute(sqlDelete, transaction);

    transaction.depth(+1);

    String insert = many.insertElementCollection();
    SqlUpdate sqlInsert = server.createSqlUpdate(insert);

    for (Map.Entry<?, ?> entry : entries) {
      sqlInsert.setParameter(1, parentId);
      sqlInsert.setParameter(2, entry.getKey());
      sqlInsert.setParameter(3, entry.getValue());
      server.execute(sqlInsert, transaction);
    }

    transaction.depth(-1);
  }
}
