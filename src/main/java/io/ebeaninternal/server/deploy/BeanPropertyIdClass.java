package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanPropertyAssocOne;

/**
 * Bean property for an IdClass embeddedId.
 */
public class BeanPropertyIdClass extends BeanPropertyAssocOne {

  public BeanPropertyIdClass(BeanDescriptorMap owner, BeanDescriptor descriptor, DeployBeanPropertyAssocOne deploy) {
    super(owner, descriptor, deploy);
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    bean._ebean_getIntercept().setOwnerId(value);
  }

  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
    bean._ebean_getIntercept().setOwnerId(value);
  }

  @Override
  public Object getValue(EntityBean bean) {
    return bean._ebean_getIntercept().getOwnerId();
  }

  @Override
  public Object getValueIntercept(EntityBean bean) {
    return bean._ebean_getIntercept().getOwnerId();
  }

}
