package io.ebean.plugin;

import java.util.Collection;

/**
 * General deployment information. This is used in {@link CustomDeployParser}.
 *
 * @author Roland Praml, FOCONIS AG
 */
public interface DeployBeanDescriptorMeta {

  /**
   * Return a collection of all BeanProperty deployment information.
   */
  public Collection<? extends DeployBeanPropertyMeta> propertiesAll();

  /**
   * Get a BeanProperty by its name.
   */
  public DeployBeanPropertyMeta getBeanProperty(String secondaryBeanName);

  /**
   * Return the DeployBeanDescriptorMeta for the given bean class.
   */
  public DeployBeanDescriptorMeta getDeployBeanDescriptorMeta(Class<?> propertyType);

  /**
   * Returns the discriminator column, if any.
   * @return
   */
  public String getDiscriminatorColumn();

  public String getBaseTable();

  DeployBeanPropertyMeta idProperty();
}
