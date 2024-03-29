package io.ebeaninternal.server.persist;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Request object used for processing the merge.
 */
final class MergeRequest {

  private final EntityBean bean;
  private final EntityBean outline;
  private final MergeContext context;

  MergeRequest(MergeContext context, EntityBean bean, EntityBean outline) {
    this.context = context;
    this.bean = bean;
    this.outline = outline;
  }

  /**
   * Return the associated server.
   */
  SpiEbeanServer getServer() {
    return context.getServer();
  }

  /**
   * Return the associated transaction.
   */
  public SpiTransaction getTransaction() {
    return context.getTransaction();
  }

  /**
   * Create a sub request with the given beans (to cascade the processing).
   */
  public MergeRequest sub(EntityBean entityBean, EntityBean outlineBean) {
    return new MergeRequest(context, entityBean, outlineBean);
  }

  /**
   * Return the entity bean being merged.
   */
  public EntityBean getBean() {
    return bean;
  }

  /**
   * Return the outline bean (only has Id property).
   */
  public EntityBean getOutline() {
    return outline;
  }

  /**
   * Add a bean to the deletion list.
   */
  public void addDelete(EntityBean deleteRemain) {
    context.addDelete(deleteRemain);
  }

  /**
   * Return true if the Ids are generated by the client. This means we can't know if a bean
   * should be inserted or updated based on having an Id value.
   */
  public boolean isClientGeneratedIds() {
    return context.isClientGeneratedIds();
  }

  /**
   * Return true if a bean of the type with the given Id exists in the database.
   */
  public boolean idExists(Class<?> beanType, Object beanId) {
    return context.idExists(beanType, beanId);
  }

}
