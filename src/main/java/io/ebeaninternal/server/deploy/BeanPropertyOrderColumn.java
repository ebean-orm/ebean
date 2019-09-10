package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Bean property for synthetic sort order value / order column.
 *
 * The value of which is held on the entity bean intercept.
 */
public class BeanPropertyOrderColumn extends BeanProperty {

  public BeanPropertyOrderColumn(BeanDescriptor<?> descriptor, DeployBeanProperty deploy) {
    super(descriptor, deploy);
  }

  @Override
  public Object getValue(EntityBean bean) {
    return bean._ebean_getIntercept().getSortOrder();
  }

  @Override
  public Object getValueIntercept(EntityBean bean) {
    return bean._ebean_getIntercept().getSortOrder();
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    bean._ebean_getIntercept().setSortOrder((int)value);
  }

  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
    bean._ebean_getIntercept().setSortOrder((int)value);
  }
}
