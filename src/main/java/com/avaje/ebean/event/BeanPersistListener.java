package com.avaje.ebean.event;

import java.util.Set;

import com.avaje.ebean.config.ServerConfig;

/**
 * Listens for committed bean events.
 * <p>
 * These listen events occur after a successful commit. They also occur in a
 * background thread rather than the thread used to perform the actual insert
 * update or delete. In this way there is a delay between the commit and when
 * the listener is notified of the event.
 * </p>
 * <p>
 * For a cluster these events may need to be broadcast. Each of the inserted(),
 * updated() and deleted() methods return true if you want those events to be
 * broadcast to the other members of a cluster (the id values are broadcast). If
 * these methods return false then the events are not broadcast.
 * </p>
 * <p>
 * It is worth noting that BeanPersistListener is different in three main ways
 * from BeanPersistController postXXX methods.
 * <ul>
 * <li>BeanPersistListener only sees successfully committed events.
 * BeanPersistController pre and post methods occur before the commit or a
 * rollback and will see events that are later rolled back</li>
 * <li>BeanPersistListener runs in a background thread and will not effect the
 * response time of the actual persist where as BeanPersistController code will</li>
 * <li>BeanPersistListener can be notified of events from other servers in a
 * cluster.</li>
 * </ul>
 * </p>
 * <p>
 * A BeanPersistListener is either found automatically via class path search or
 * can be added programmatically via {@link ServerConfig#add(BeanPersistListener)}}.
 * </p>
 * @see ServerConfig#add(BeanPersistListener)
 */
public interface BeanPersistListener {

  /**
   * Return true if this BeanPersistListener should be registered for events
   * on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Notified that a bean has been inserted locally. Return true if you want the
   * cluster to be notified of the event.
   * 
   * @param bean
   *          The bean that was inserted.
   */
  boolean inserted(Object bean);

  /**
   * Notified that a bean has been updated locally. Return true if you want the
   * cluster to be notified of the event.
   * 
   * @param bean
   *          The bean that was updated.
   * @param updatedProperties
   *          The properties that were modified by this update.
   */
  boolean updated(Object bean, Set<String> updatedProperties);

  /**
   * Notified that a bean has been deleted locally. Return true if you want the
   * cluster to be notified of the event.
   * 
   * @param bean
   *          The bean that was deleted.
   */
  boolean deleted(Object bean);

  /**
   * Notify that a bean was inserted on another node of the cluster.
   * 
   * @param id
   *          the id value of the inserted bean
   */
  void remoteInsert(Object id);

  /**
   * Notify that a bean was updated on another node of the cluster.
   * 
   * @param id
   *          the id value of the updated bean.
   */
  void remoteUpdate(Object id);

  /**
   * Notify that a bean was deleted on another node of the cluster.
   * 
   * @param id
   *          the id value of the deleted bean.
   */
  void remoteDelete(Object id);

}
