package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;

/**
 * Bean descriptor used with ElementCollection (where we don't have a mapped type/class).
 */
public class BeanDescriptorElement<T> extends BeanDescriptor<T> {

  public BeanDescriptorElement(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy) {
    super(owner, deploy);
  }

  public boolean isElementType() {
    return true;
  }

  @Override
  protected EntityBean createPrototypeEntityBean(Class<T> beanType) {
    return new ElementEntityBean(properties);
  }

}
