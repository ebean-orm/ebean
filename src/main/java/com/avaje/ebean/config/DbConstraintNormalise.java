package com.avaje.ebean.config;

/**
 * Used to normalise table and column names which means stripping out
 * quoted identifier characters and any catalog or schema prefix.
 */
public class DbConstraintNormalise {

  protected final String[] quotedIdentifiers;

  protected final boolean lowerCaseTables;

  protected final boolean lowerCaseColumns;

  public DbConstraintNormalise() {
    this(true, true);
  }

  public DbConstraintNormalise(boolean lowerCaseTables, boolean lowerCaseColumns) {
    this.lowerCaseTables = lowerCaseTables;
    this.lowerCaseColumns = lowerCaseColumns;
    this.quotedIdentifiers = new String[]{"\"", "'", "[", "]", "`"};
  }

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  public String normaliseTable(String tableName) {

    tableName = trimQuotes(tableName);
    int lastPeriod = tableName.lastIndexOf('.');
    if (lastPeriod > -1) {
      // trim off catalog and schema prefix
      tableName = tableName.substring(lastPeriod + 1);
    }
    if (lowerCaseTables) {
      tableName = tableName.toLowerCase();
    }
    return tableName;
  }

  /**
   * Normalise the column name by removing any quoted identifier characters.
   */
  public String normaliseColumn(String columnName) {

    columnName = trimQuotes(columnName);
    if (lowerCaseColumns) {
      columnName = columnName.toLowerCase();
    }
    return columnName;
  }

  /**
   * Lower case the table name checking for quoted identifiers.
   */
  public String lowerTableName(String tableName) {
    if (lowerCaseTables && notQuoted(tableName)) {
      return tableName.toLowerCase();
    }
    return tableName;
  }

  /**
   * Lower case the column name checking for quoted identifiers.
   */
  public String lowerColumnName(String name) {
    if (lowerCaseColumns && notQuoted(name)) {
      return name.toLowerCase();
    }
    return name;
  }

  /**
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  public boolean notQuoted(String tableName) {

    // remove quoted identifier characters
    for (int i = 0; i < quotedIdentifiers.length; i++) {
      if (tableName.contains(quotedIdentifiers[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  public String trimQuotes(String tableName) {

    if (tableName == null) {
      return "";
    }
    // remove quoted identifier characters
    for (int i = 0; i < quotedIdentifiers.length; i++) {
      tableName = tableName.replace(quotedIdentifiers[i], "");
    }
    return tableName;
  }


}
