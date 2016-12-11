package io.ebeaninternal.server.core;

import io.ebean.SqlUpdate;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.id.ImportedId;

/**
 * Deferred update of a relationship where an Id value is not initially available
 * so instead we execute this later as a SqlUpdate statement.
 */
public class PersistDeferredRelationship {

  private final SpiEbeanServer ebeanServer;
  private final BeanDescriptor<?> beanDescriptor;
  private final EntityBean assocBean;
  private final ImportedId importedId;
  private final EntityBean bean;

  public PersistDeferredRelationship(SpiEbeanServer ebeanServer, BeanDescriptor<?> beanDescriptor, EntityBean assocBean, ImportedId importedId, EntityBean bean) {

    this.ebeanServer = ebeanServer;
    this.beanDescriptor = beanDescriptor;
    this.assocBean = assocBean;
    this.importedId = importedId;
    this.bean = bean;
  }

  /**
   * Build and execute a SqlUpdate to set the importId value (as it will be available now).
   * <p>
   * This is executed later (deferred) until after JDBC batch flush or prior to commit.
   * </p>
   */
  public void execute(SpiTransaction transaction) {

    String sql = beanDescriptor.getUpdateImportedIdSql(importedId);
    SqlUpdate sqlUpdate = ebeanServer.createSqlUpdate(sql);

    // bind the set clause for the importedId
    int pos = importedId.bind(1, sqlUpdate, assocBean);

    // bind the where clause for the bean
    Object[] idValues = beanDescriptor.getIdBinder().getIdValues(bean);
    for (int j = 0; j < idValues.length; j++) {
      sqlUpdate.setParameter(pos + j, idValues[j]);
    }

    ebeanServer.execute(sqlUpdate, transaction);
  }
}
