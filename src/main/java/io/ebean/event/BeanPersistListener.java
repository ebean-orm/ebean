package io.ebean.event;

import io.ebean.config.ServerConfig;

import java.util.Set;

/**
 * Listens for committed bean events.
 * <p>
 * These listen events occur after a successful commit. They also occur in a
 * background thread rather than the thread used to perform the actual insert
 * update or delete. In this way there is a delay between the commit and when
 * the listener is notified of the event.
 * </p>
 * <p>
 * It is worth noting that BeanPersistListener is different in two main ways
 * from BeanPersistController postXXX methods.
 * <ul>
 * <li>
 * BeanPersistListener only sees successfully committed events.
 * BeanPersistController pre and post methods occur before the commit or a
 * rollback and will see events that are later rolled back
 * </li>
 * <li>
 * BeanPersistListener runs in a background thread and will not effect the
 * response time of the actual persist where as BeanPersistController code will
 * </li>
 * </ul>
 * </p>
 * <p>
 * A BeanPersistListener is either found automatically via class path search or
 * can be added programmatically via {@link ServerConfig#add(BeanPersistListener)}}.
 * </p>
 *
 * @see ServerConfig#add(BeanPersistListener)
 */
public interface BeanPersistListener {

  /**
   * Return true if this BeanPersistListener should be registered for events
   * on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Notified that a bean has been inserted.
   *
   * @param bean The bean that was inserted.
   */
  void inserted(Object bean);

  /**
   * Notified that a bean has been updated.
   *
   * @param bean              The bean that was updated.
   * @param updatedProperties The properties that were modified by this update.
   */
  void updated(Object bean, Set<String> updatedProperties);

  /**
   * Notified that a bean has been deleted.
   *
   * @param bean The bean that was deleted.
   */
  void deleted(Object bean);

  /**
   * Notified that a bean has been soft deleted.
   *
   * @param bean The bean that was soft deleted.
   */
  void softDeleted(Object bean);

}
