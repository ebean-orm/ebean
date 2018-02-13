package io.ebean.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface DeployBeanPropertyMeta {

  /**
   * Return the name of the property.
   */
  String getName();
  
  /**
   * The database column name this is mapped to.
   */
  String getDbColumn();

  /**
   * Return the bean Field associated with this property.
   */
  Field getField();
  
  /**
   * Return the getter method.
   */
  public Method getReadMethod();

  /**
   * The property is based on a formula.
   */
  void setSqlFormula(String sqlSelect, String sqlJoin);

  /**
   * Return the bean type.
   */
  Class<?> getOwningType();

  /**
   * Return the property type.
   */
  Class<?> getPropertyType();
  
}
