package io.ebeaninternal.server.deploy.meta;

import io.ebeaninternal.server.deploy.BeanDescriptorMap;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

/**
 * Used for associated beans in place of a BeanDescriptor. This is done to avoid
 * recursion issues due to the potentially bi-directional and circular
 * relationships between beans.
 * <p>
 * It holds the main deployment information and not all the detail that is held
 * in a BeanDescriptor.
 * </p>
 */
public class DeployBeanTable {

  private final Class<?> beanType;

  /**
   * The base table.
   */
  private String baseTable;

  private DeployBeanProperty idProperty;

  /**
   * Create the BeanTable.
   */
  DeployBeanTable(Class<?> beanType) {
    this.beanType = beanType;
  }

  /**
   * Return the base table for this BeanTable.
   * This is used to determine the join information
   * for associations.
   */
  public String getBaseTable() {
    return baseTable;
  }

  /**
   * Set the base table for this BeanTable.
   */
  public void setBaseTable(String baseTable) {
    this.baseTable = baseTable;
  }

  /**
   * Return the id properties.
   */
  public BeanProperty createIdProperty(BeanDescriptorMap owner) {
    return idProperty == null ? null : createProperty(owner, idProperty);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private BeanProperty createProperty(BeanDescriptorMap owner, DeployBeanProperty prop) {

    if (prop instanceof DeployBeanPropertyAssocOne<?>) {
      return new BeanPropertyAssocOne(owner, (DeployBeanPropertyAssocOne<?>) prop);

    } else {
      return new BeanProperty(prop);
    }
  }

  /**
   * Set the Id properties.
   */
  public void setIdProperty(DeployBeanProperty idProperty) {
    this.idProperty = idProperty;
  }

  /**
   * Return the class for this beanTable.
   */
  public Class<?> getBeanType() {
    return beanType;
  }

}
