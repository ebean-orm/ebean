package com.avaje.ebean.config;

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
  public TableName getTableNameByConvention(Class<?> beanClass) {

    return new TableName(getCatalog(), getSchema(), toUnderscoreFromCamel(beanClass.getSimpleName()));
  }

  /**
   * Converts Camel case property name to underscore based column name.
   *
   * @return the column from property
   */
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

  /**
   * Convert and return the string to underscore from camel case.
   */
  protected String toUnderscoreFromCamel(String camelCase) {

    int lastUpper = -1;
    StringBuilder sb = new StringBuilder(camelCase.length()+4);
    for (int i = 0; i < camelCase.length(); i++) {
      char c = camelCase.charAt(i);

      if ('_' == c) {
        // Underscores should just be passed through
        sb.append(c);
        lastUpper = i;
      } else if (Character.isDigit(c)) {
        if (i > lastUpper + 1 && !digitsCompressed) {
          sb.append("_");
        }
        sb.append(c);
        lastUpper = i;

      } else if (Character.isUpperCase(c)) {
        if (i > lastUpper + 1) {
          sb.append("_");
        }
        sb.append(Character.toLowerCase(c));
        lastUpper = i;

      } else {
        sb.append(c);
      }
    }
    String ret = sb.toString();
    if (forceUpperCase) {
      ret = ret.toUpperCase();
    }
    return ret;
  }

  /**
   * Convert and return the from string from underscore to camel case.
   */
  protected String toCamelFromUnderscore(String underscore) {

    StringBuilder result = new StringBuilder(underscore.length());
    String[] vals = underscore.split("_");

    for (int i = 0; i < vals.length; i++) {
      String lower = vals[i].toLowerCase();
      if (i > 0) {
        char c = Character.toUpperCase(lower.charAt(0));
        result.append(c);
        result.append(lower.substring(1));
      } else {
        result.append(lower);
      }
    }

    return result.toString();
  }
}
