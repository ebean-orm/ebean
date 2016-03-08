package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;

/**
 * A BeanProperty owned by a Compound value object that maps to
 * a real scalar type.
 */
public class BeanPropertyCompoundScalar extends BeanProperty {

  private final BeanPropertyCompoundRoot rootProperty;

  private final CtCompoundProperty ctProperty;

  public BeanPropertyCompoundScalar(BeanPropertyCompoundRoot rootProperty, DeployBeanProperty scalarDeploy, CtCompoundProperty ctProperty) {
    super(scalarDeploy);
    this.rootProperty = rootProperty;
    this.ctProperty = ctProperty;
  }

  /**
   * Return one of the scalar values from a compound type.
   */
  @SuppressWarnings("unchecked")
  public Object getValueObject(Object compoundValue) {
    return ctProperty.getValue(compoundValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object getValue(EntityBean valueObject) {
    return ctProperty.getValue(valueObject);
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    setValueInCompound(bean, value, false);
  }

  @SuppressWarnings("unchecked")
  public void setValueInCompound(EntityBean bean, Object value, boolean intercept) {

    Object compoundValue = ctProperty.setValue(bean, value);

    if (compoundValue != null) {
      // we are at the top level and we have a compound value
      // that we can set using the root property
      if (intercept) {
        rootProperty.setRootValueIntercept(bean, compoundValue);
      } else {
        rootProperty.setRootValue(bean, compoundValue);
      }
    }
  }

  /**
   * No interception on embedded scalar values inside a CVO.
   */
  @Override
  public void setValueIntercept(EntityBean bean, Object value) {
    setValueInCompound(bean, value, true);
  }

  /**
   * No interception on embedded scalar values inside a CVO.
   */
  @Override
  public Object getValueIntercept(EntityBean bean) {
    return getValue(bean);
  }

  @Override
  public Object pathGetNested(Object bean) {
    return pathGet(bean);
  }

  @Override
  public Object pathGet(Object bean) {
    return ctProperty.getValue(bean);
  }

}
