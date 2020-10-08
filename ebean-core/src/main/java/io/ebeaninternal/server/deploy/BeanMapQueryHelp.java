package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanMap;
import io.ebeaninternal.server.el.ElPropertyValue;
import io.ebeaninternal.server.query.CQueryCollectionAdd;

class BeanMapQueryHelp<T> implements CQueryCollectionAdd<T> {

  private final ElPropertyValue elGetValue;

  /**
   * Create for a findMap query.
   */
  BeanMapQueryHelp(ElPropertyValue elGetValue) {
    this.elGetValue = elGetValue;
  }

  @Override
  public BeanCollection<T> createEmptyNoParent() {
    return new BeanMap<>();
  }

  @Override
  public void add(BeanCollection collection, EntityBean bean, boolean withCheck) {
    if (bean == null) {
      ((BeanMap<?, ?>) collection).internalPutNull();
    } else {
      Object keyValue = elGetValue.pathGet(bean);
      BeanMap<?, ?> map = ((BeanMap<?, ?>) collection);
      map.internalPutWithCheck(keyValue, bean);
    }
  }
}
