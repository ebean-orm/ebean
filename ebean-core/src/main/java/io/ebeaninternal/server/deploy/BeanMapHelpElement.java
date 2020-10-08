package io.ebeaninternal.server.deploy;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.common.BeanMap;
import io.ebeaninternal.api.json.SpiJsonWriter;

public class BeanMapHelpElement<T> extends BeanMapHelp<T> {

  BeanMapHelpElement(BeanPropertyAssocMany<T> many) {
    super(many);
  }

  @Override
  public void add(BeanCollection<?> collection, EntityBean bean, boolean withCheck) {
    Object key = bean._ebean_getField(0);
    Object val = bean._ebean_getField(1);

    BeanMap<?, ?> map = ((BeanMap<?, ?>) collection);
    if (withCheck) {
      map.internalPutWithCheck(key, val);
    } else {
      map.internalPut(key, val);
    }
  }

  @Override
  void jsonWriteElement(SpiJsonWriter ctx, Object element) {
    throw new IllegalStateException("not called");
  }
}
