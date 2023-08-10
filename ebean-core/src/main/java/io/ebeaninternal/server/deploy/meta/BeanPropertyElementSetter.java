package io.ebeaninternal.server.deploy.meta;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.properties.BeanPropertySetter;

/**
 * Setter used for "element beans" with ElementCollection.
 */
final class BeanPropertyElementSetter implements BeanPropertySetter {

  private final int pos;

  BeanPropertyElementSetter(int pos) {
    this.pos = pos;
  }

  @Override
  public void set(EntityBean bean, Object value) {
    bean._ebean_getIntercept().setValue(pos, value);
  }

  @Override
  public void setIntercept(EntityBean bean, Object value) {
    set(bean, value);
  }
}
