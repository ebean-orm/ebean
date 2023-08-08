package io.ebeaninternal.api;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;

/**
 * A buffer of bean collections for batch lazy loading and secondary query loading.
 */
public interface LoadManyBuffer {

  /**
   * The batch (max) size;
   */
  int batchSize();

  /**
   * The actual size.
   */
  int size();

  /**
   * Get the <code>i</code>th element from buffer. This can be null.
   */
  BeanCollection<?> get(int i);

  /**
   * Removes an element from the buffer. This will NOT affect size.
   */
  boolean removeFromBuffer(BeanCollection<?> collection);

  BeanPropertyAssocMany<?> beanProperty();

  ObjectGraphNode objectGraphNode();

  BeanDescriptor<?> descriptor();

  PersistenceContext persistenceContext();

  String fullPath();

  void configureQuery(SpiQuery<?> query);

  boolean isUseDocStore();
}
