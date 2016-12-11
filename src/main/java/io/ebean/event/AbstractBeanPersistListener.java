package io.ebean.event;

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
   * Notified that a bean has been inserted.
   *
   * @param bean The bean that was inserted.
   */
  @Override
  public void inserted(Object bean) {
  }

  /**
   * Notified that a bean has been updated.
   *
   * @param bean              The bean that was updated.
   * @param updatedProperties The properties that were modified by this update.
   */
  @Override
  public void updated(Object bean, Set<String> updatedProperties) {
  }

  /**
   * Notified that a bean has been deleted.
   *
   * @param bean The bean that was deleted.
   */
  @Override
  public void deleted(Object bean) {
  }

  /**
   * Notified that a bean has been soft deleted.
   *
   * @param bean The bean that was deleted.
   */
  @Override
  public void softDeleted(Object bean) {
  }
}
