package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;
import com.avaje.ebeaninternal.server.type.CtCompoundProperty;

/**
 * A BeanProperty owned by a Compound value object that maps to
 * a real scalar type.
 */
public class BeanPropertyCompoundScalar extends BeanProperty {

  private final BeanPropertyCompoundRoot rootProperty;

  private final CtCompoundProperty ctProperty;

  @SuppressWarnings("rawtypes")
  private final ScalarTypeConverter typeConverter;

  public BeanPropertyCompoundScalar(BeanPropertyCompoundRoot rootProperty, DeployBeanProperty scalarDeploy,
                                    CtCompoundProperty ctProperty, ScalarTypeConverter<?, ?> typeConverter) {

    super(scalarDeploy);
    this.rootProperty = rootProperty;
    this.ctProperty = ctProperty;
    this.typeConverter = typeConverter;
  }

  /**
   * Return one of the scalar values from a compound type.
   */
  public Object getValueObject(Object compoundValue) {
    if (typeConverter != null) {
      compoundValue = typeConverter.unwrapValue(compoundValue);
    }
    return ctProperty.getValue(compoundValue);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object getValue(EntityBean valueObject) {
    Object val = valueObject;
    if (typeConverter != null) {
      val = typeConverter.unwrapValue(val);
    }
    return ctProperty.getValue(val);
  }

  @Override
  public void setValue(EntityBean bean, Object value) {
    setValueInCompound(bean, value, false);
  }

  @SuppressWarnings("unchecked")
  public void setValueInCompound(EntityBean bean, Object value, boolean intercept) {

    Object compoundValue = ctProperty.setValue(bean, value);

    if (compoundValue != null) {
      if (typeConverter != null) {
        compoundValue = typeConverter.wrapValue(compoundValue);
      }
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
  public Object elGetReference(EntityBean bean) {
    return getValue(bean);
  }

  @Override
  public Object elGetValue(EntityBean bean) {
    return getValue(bean);
  }

  @Override
  public void elSetValue(EntityBean bean, Object value, boolean populate) {//, boolean reference) {
    super.elSetValue(bean, value, populate);
  }

}
