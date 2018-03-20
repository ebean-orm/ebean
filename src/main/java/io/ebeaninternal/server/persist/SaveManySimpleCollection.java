package io.ebeaninternal.server.persist;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

class SaveManySimpleCollection extends SaveManyPropRequest {

  SaveManySimpleCollection(boolean insertedParent, BeanPropertyAssocMany<?> many, EntityBean parentBean, PersistRequestBean<?> request) {
    super(insertedParent, many, parentBean, request);
  }

  @Override
  void processDetails() {

    Object parentId = request.getBeanId();

    SqlUpdate sqlDelete = many.deleteByParentId(parentId, null);

    SpiEbeanServer server = request.getServer();
    server.execute(sqlDelete, transaction);

    transaction.depth(+1);

    String insert = many.insertElementCollection();
    SqlUpdate sqlInsert = server.createSqlUpdate(insert);

    for (Object value : collection) {

      sqlInsert.setParameter(1, parentId);
      sqlInsert.setParameter(2, value);
      server.execute(sqlInsert, transaction);
    }

    transaction.depth(-1);
  }
}
