package com.avaje.ebean.config;

/**
 * The JPA naming convention where column names match property names and table
 * names match entity names.
 * 
 * <p>
 * The JPA specification states that the in the case of no annotations the name
 * of the class will be take as the table name and the name of a property will
 * be taken as the name of the column.
 * </p>
 * 
 * @author emcgreal
 */
public class MatchingNamingConvention extends AbstractNamingConvention {

  /**
   * Create with a sequence format of "{table}_seq".
   */
  public MatchingNamingConvention() {
    super();
  }

  /**
   * Instantiates with a specific format for DB sequences.
   * 
   * @param sequenceFormat
   *          the sequence format
   */
  public MatchingNamingConvention(String sequenceFormat) {
    super(sequenceFormat);
  }

  public String getColumnFromProperty(Class<?> beanClass, String propertyName) {
    return propertyName;
  }

  public TableName getTableNameByConvention(Class<?> beanClass) {

    return new TableName(getCatalog(), getSchema(), beanClass.getSimpleName());
  }

  public String getPropertyFromColumn(Class<?> beanClass, String dbColumnName) {
    return dbColumnName;
  }
}
