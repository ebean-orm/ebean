package io.ebean.config;

import io.ebean.util.CamelCaseHelper;

/**
 * Converts between Camel Case and Underscore based names for both table and
 * column names (and is the default naming convention in Ebean).
 *
 * @author emcgreal
 * @author rbygrave
 */
public class UnderscoreNamingConvention extends AbstractNamingConvention {

  /**
   * Force toUnderscore to return in upper case.
   */
  private boolean forceUpperCase = false;

  /**
   * The digits compressed.
   */
  private boolean digitsCompressed = true;

  /**
   * Create with a given sequence format.
   *
   * @param sequenceFormat the sequence format
   */
  public UnderscoreNamingConvention(String sequenceFormat) {
    super(sequenceFormat);
  }

  /**
   * Create with a sequence format of "{table}_seq".
   */
  public UnderscoreNamingConvention() {
    super();
  }

  /**
   * Returns the last part of the class name.
   *
   * @param beanClass the bean class
   * @return the table name from class
   */
  @Override
  public TableName getTableNameByConvention(Class<?> beanClass) {

    return new TableName(getCatalog(), getSchema(), toUnderscoreFromCamel(beanClass.getSimpleName()));
  }

  /**
   * Converts Camel case property name to underscore based column name.
   *
   * @return the column from property
   */
  @Override
  public String getColumnFromProperty(Class<?> beanClass, String propertyName) {

    return toUnderscoreFromCamel(propertyName);
  }

  /**
   * Converts underscore based column name to Camel case property name.
   *
   * @param beanClass    the bean class
   * @param dbColumnName the db column name
   * @return the property from column
   */
  @Override
  public String getPropertyFromColumn(Class<?> beanClass, String dbColumnName) {
    return toCamelFromUnderscore(dbColumnName);
  }

  /**
   * Return true if the result will be upper case.
   * <p>
   * False if it will be lower case.
   * </p>
   */
  public boolean isForceUpperCase() {
    return forceUpperCase;
  }

  /**
   * Set to true to make the result upper case.
   */
  public void setForceUpperCase(boolean forceUpperCase) {
    this.forceUpperCase = forceUpperCase;
  }

  /**
   * Returns true if digits are compressed.
   */
  public boolean isDigitsCompressed() {
    return digitsCompressed;
  }

  /**
   * Sets to true for digits to be compressed (without a leading underscore).
   */
  public void setDigitsCompressed(boolean digitsCompressed) {
    this.digitsCompressed = digitsCompressed;
  }

  @Override
  public String getForeignKey(String prefix, String fkProperty) {
    return prefix + "_" + toUnderscoreFromCamel(fkProperty);
  }

  /**
   * Convert and return the string to underscore from camel case.
   */
  protected String toUnderscoreFromCamel(String camelCase) {
    return CamelCaseHelper.toUnderscoreFromCamel(camelCase, digitsCompressed, forceUpperCase);
  }

  /**
   * Convert and return the from string from underscore to camel case.
   */
  protected String toCamelFromUnderscore(String underscore) {
    return CamelCaseHelper.toCamelFromUnderscore(underscore);
  }
}
