package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebean.PersistenceContextScope;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.transaction.DefaultPersistenceContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for deletion of a partially populated bean where some cascade delete
 * properties where not loaded.
 * <p>
 * This bean effectively holds the foreign properties that where not loaded, and
 * helps fetch the foreign keys and delete the appropriate rows.
 * </p>
 */
class DeleteUnloadedForeignKeys {

  private final List<BeanPropertyAssocOne<?>> propList = new ArrayList<BeanPropertyAssocOne<?>>(4);

  private final SpiEbeanServer server;

  private final PersistRequestBean<?> request;

  private final boolean deletePermanent;

  private EntityBean beanWithForeignKeys;

  DeleteUnloadedForeignKeys(SpiEbeanServer server, PersistRequestBean<?> request) {
    this.server = server;
    this.request = request;
    this.deletePermanent = request.isHardDeleteCascade();
  }

  public boolean isEmpty() {
    return propList.isEmpty();
  }

  public void add(BeanPropertyAssocOne<?> prop) {
    propList.add(prop);
  }

  /**
   * Execute a query fetching the missing (unloaded) foreign keys. We need to
   * fetch these key values before the parent bean is deleted.
   */
  void queryForeignKeys() {

    BeanDescriptor<?> descriptor = request.getBeanDescriptor();
    SpiQuery<?> q = (SpiQuery<?>) server.createQuery(descriptor.getBeanType());

    Object id = request.getBeanId();

    StringBuilder sb = new StringBuilder(30);
    for (int i = 0; i < propList.size(); i++) {
      sb.append(propList.get(i).getName()).append(",");
    }

    // run query in a separate persistence context
    q.setPersistenceContext(new DefaultPersistenceContext());
    q.setPersistenceContextScope(PersistenceContextScope.QUERY);
    q.setAutoTune(false);
    q.select(sb.toString());
    q.setIncludeSoftDeletes();
    q.where().idEq(id);

    SpiTransaction t = request.getTransaction();
    if (t.isLogSummary()) {
      t.logSummary("-- Ebean fetching foreign key values for delete of " + descriptor.getName() + " id:" + id);
    }
    beanWithForeignKeys = (EntityBean) server.findUnique(q, t);
  }

  /**
   * Delete the rows relating to the foreign keys. These deletions occur after
   * the parent bean has been deleted.
   */
  void deleteCascade() {

    for (int i = 0; i < propList.size(); i++) {
      BeanPropertyAssocOne<?> prop = propList.get(i);
      Object detailBean = prop.getValue(beanWithForeignKeys);

      // if bean exists with a unique id then delete it
      if (detailBean != null && prop.hasId((EntityBean) detailBean)) {
        if (deletePermanent) {
          server.deletePermanent(detailBean, request.getTransaction());
        } else {
          server.delete(detailBean, request.getTransaction());
        }
      }
    }
  }
}
