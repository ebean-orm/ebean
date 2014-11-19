package com.avaje.ebean.event;

import java.util.Set;

/**
 * Provides a base implementation of BeanPersistListener.
 * <p>
 * Objects extending this should override the methods then are interested in.
 * The default inserted() updated() and deleted() methods return false and as such
 * means other servers in the cluster are not notified.
 * </p>
 */
public abstract class AbstractBeanPersistListener implements BeanPersistListener {

  /**
   * Notified that a bean has been inserted locally. Return true if you want the
   * cluster to be notified of the event.
   *
   * @param bean The bean that was inserted.
   */
  @Override
  public boolean inserted(Object bean) {
    return false;
  }

  /**
   * Notified that a bean has been updated locally. Return true if you want the
   * cluster to be notified of the event.
   *
   * @param bean              The bean that was updated.
   * @param updatedProperties The properties that were modified by this update.
   */
  @Override
  public boolean updated(Object bean, Set<String> updatedProperties) {
    return false;
  }

  /**
   * Notified that a bean has been deleted locally. Return true if you want the
   * cluster to be notified of the event.
   *
   * @param bean The bean that was deleted.
   */
  @Override
  public boolean deleted(Object bean) {
    return false;
  }

  /**
   * Notify that a bean was inserted on another node of the cluster.
   *
   * @param id the id value of the inserted bean
   */
  @Override
  public void remoteInsert(Object id) {
    // do nothing
  }

  /**
   * Notify that a bean was updated on another node of the cluster.
   *
   * @param id the id value of the updated bean.
   */
  @Override
  public void remoteUpdate(Object id) {
    // do nothing
  }

  /**
   * Notify that a bean was deleted on another node of the cluster.
   *
   * @param id the id value of the deleted bean.
   */
  @Override
  public void remoteDelete(Object id) {
    // do nothing
  }
}
