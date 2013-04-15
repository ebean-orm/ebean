package com.avaje.ebean.config;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;

/**
 * Defines the naming convention for converting between logical property
 * names/entity names and physical DB column names/table names.
 * <p>
 * The main goal of the naming convention is to reduce the amount of
 * configuration required in the mapping (especially when mapping between column
 * and property names).
 * </p>
 * <p>
 * Note that if you do not define a NamingConvention the default one will be
 * used and you can configure it's behaviour via properties.
 * </p>
 */
public interface NamingConvention {

  /**
   * Set the associated DatabasePlaform.
   * <p>
   * This is set after the DatabasePlatform has been associated.
   * </p>
   * <p>
   * The purpose of this is to enable NamingConvention to be able to support
   * database platform specific configuration.
   * </p>
   * 
   * @param databasePlatform
   *          the database platform
   */
  public void setDatabasePlatform(DatabasePlatform databasePlatform);

  /**
   * Returns the table name for a given Class.
   * <p>
   * This method is always called and should take into account @Table
   * annotations etc. This means you can choose to override the settings defined
   * by @Table if you wish.
   * </p>
   * 
   * @param beanClass
   *          the bean class
   * 
   * @return the table name for the entity class
   */
  public TableName getTableName(Class<?> beanClass);

  /**
   * Returns the ManyToMany join table name (aka the intersection table).
   * 
   * @param lhsTable
   *          the left hand side bean table
   * @param rhsTable
   *          the right hand side bean table
   * 
   * @return the many to many join table name
   */
  public TableName getM2MJoinTableName(TableName lhsTable, TableName rhsTable);

  /**
   * Return the column name given the property name.
   * 
   * @return the column name for a given property
   */
  public String getColumnFromProperty(Class<?> beanClass, String propertyName);

  /**
   * Return the property name from the column name.
   * <p>
   * This is used to help mapping of raw SQL queries onto bean properties.
   * </p>
   * 
   * @param beanClass
   *          the bean class
   * @param dbColumnName
   *          the db column name
   * 
   * @return the property name from the column name
   */
  public String getPropertyFromColumn(Class<?> beanClass, String dbColumnName);

  /**
   * Return the sequence name given the table name (for DB's that use
   * sequences).
   * <p>
   * Typically you might append "_seq" to the table name as an example.
   * </p>
   * 
   * @param tableName
   *          the table name
   * 
   * @return the sequence name
   */
  public String getSequenceName(String tableName, String pkColumn);

  /**
   * Return true if a prefix should be used building a foreign key name.
   * <p>
   * This by default is true and this works well when the primary key column
   * names are simply "ID". In this case a prefix (such as "ORDER" and
   * "CUSTOMER" etc) is added to the foreign key column producing "ORDER_ID" and
   * "CUSTOMER_ID".
   * </p>
   * <p>
   * This should return false when your primary key columns are the same as the
   * foreign key columns. For example, when the primary key columns are
   * "ORDER_ID", "CUST_ID" etc ... and they are the same as the foreign key
   * column names.
   * </p>
   */
  public boolean isUseForeignKeyPrefix();

}