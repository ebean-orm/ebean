package io.ebeaninternal.server.query;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;

/**
 * A NOOP based CQueryCollectionAdd for use with lazy loading many queries where the
 * beans loaded into the collection are added to the collection(s) of the parent(s).
 */
class CQueryCollectionAddNoop<T> implements CQueryCollectionAdd<T> {

  /**
   * Return null as we are not collecting the beans.
   */
  @Override
  public BeanCollection<T> createEmptyNoParent() {
    return null;
  }

  /**
   * Do nothing for this case.
   */
  @Override
  public void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck) {
    // do nothing
  }
}
