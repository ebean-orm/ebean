package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.type.ScalarType;

import java.util.Arrays;

/**
 * Bean descriptor used with ElementCollection (where we don't have a mapped type/class).
 * <p>
 * This is somewhat a BeanDescriptor created 'on the fly' for a specific element collection property
 * with a unidirectional property and mapping etc specific to the property (and not the type if embedded).
 */
abstract class BeanDescriptorElement<T> extends BeanDescriptor<T> {

  final ElementHelp elementHelp;

  BeanDescriptorElement(BeanDescriptorMap owner, DeployBeanDescriptor<T> deploy, ElementHelp elementHelp) {
    super(owner, deploy);
    this.elementHelp = elementHelp;
  }

  /**
   * Find and return the first base scalar type (and we expect only 1).
   */
  ScalarType<Object> firstBaseScalarType() {
    BeanProperty[] props = propertiesBaseScalar();
    if (props.length != 1) {
      throw new IllegalStateException("Expecting 1 property for element scalar but got " + Arrays.toString(props));
    }
    return props[0].getScalarType();
  }

  @Override
  public boolean isElementType() {
    return true;
  }

  /**
   * Our entity beans used are somewhat fake ones (ElementEntityBean) such that we hold the unidirectional property
   * value (foreign key) and the actual element collection value (scalar or embedded plus map key).
   */
  @Override
  protected EntityBean createPrototypeEntityBean(Class<T> beanType) {
    return new ElementEntityBean(properties);
  }

}
