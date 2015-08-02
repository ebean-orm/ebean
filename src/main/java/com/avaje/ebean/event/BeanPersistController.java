package com.avaje.ebean.event;

import java.util.Set;

/**
 * Used to enhance or override the default bean persistence mechanism.
 * <p>
 * Note that if want to totally change the finding, you need to use a BeanQueryAdapter
 * rather than using postLoad().
 * </p>
 * <p>
 * Note that getTransaction() on the PersistRequest returns the transaction used
 * for the insert, update, delete or fetch. To explicitly use this same
 * transaction you should use this transaction via methods on EbeanServer.
 * </p>
 * 
 * <pre class="code">
 * 
 *        Object extaBeanToSave = ...;
 *        Transaction t = request.getTransaction();
 *        EbeanServer server = request.getEbeanServer();
 *        server.save(extraBeanToSave, t);
 * 
 * </pre>
 * 
 * <p>
 * It is worth noting that BeanPersistListener is different in three main ways
 * from BeanPersistController postXXX methods.
 * <ul>
 * <li>BeanPersistListener only sees successfully committed events.
 * BeanController pre and post methods occur before the commit or a rollback and
 * will see events that are later rolled back</li>
 * <li>BeanPersistListener runs in a background thread and will not effect the
 * response time of the actual persist where as BeanController code will</li>
 * <li>BeanPersistListener can be notified of events from other servers in a
 * cluster.</li>
 * </ul>
 * </p>
 * <p>
 * A BeanPersistController is either found automatically via class path search
 * or can be added programmatically via ServerConfiguration.addEntity().
 * </p>
 */
public interface BeanPersistController {

  /**
   * When there are multiple BeanPersistController's for a given entity type
   * this controls the order in which they are executed.
   * <p>
   * Lowest values are executed first.
   * </p>
   * 
   * @return an int used to control the order BeanPersistController's are
   *         executed
   */
  int getExecutionOrder();

  /**
   * Return true if this BeanPersistController should be registered for events
   * on this entity type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Prior to the insert perform some action. Return true if you want the
   * default functionality to continue.
   * <p>
   * Return false if you have completely replaced the insert functionality and
   * do not want the default insert to be performed.
   * </p>
   */
  boolean preInsert(BeanPersistRequest<?> request);

  /**
   * Prior to the update perform some action. Return true if you want the
   * default functionality to continue.
   * <p>
   * Return false if you have completely replaced the update functionality and
   * do not want the default update to be performed.
   * </p>
   */
  boolean preUpdate(BeanPersistRequest<?> request);

  /**
   * Prior to the delete perform some action. Return true if you want the
   * default functionality to continue.
   * <p>
   * Return false if you have completely replaced the delete functionality and
   * do not want the default delete to be performed.
   * </p>
   */
  boolean preDelete(BeanPersistRequest<?> request);

  /**
   * Called after the insert was performed.
   */
  void postInsert(BeanPersistRequest<?> request);

  /**
   * Called after the update was performed.
   */
  void postUpdate(BeanPersistRequest<?> request);

  /**
   * Called after the delete was performed.
   */
  void postDelete(BeanPersistRequest<?> request);

}
