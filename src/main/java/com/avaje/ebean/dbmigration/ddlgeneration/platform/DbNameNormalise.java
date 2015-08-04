package com.avaje.ebean.dbmigration.ddlgeneration.platform;

/**
 * Used to normalise table and column names which means stripping out
 * quoted identifier characters and any catalog or schema prefix.
 */
public class DbNameNormalise {

  protected boolean lowerCase = true;

  protected String[] quotedIdentifiers = {"\"", "'", "[", "]", "`"};

  /**
   * Normalise the table name by trimming catalog and schema and removing any
   * quoted identifier characters (",',[,] etc).
   */
  public String normalise(String tableName) {

    tableName = trimQuotes(tableName);
    int lastPeriod = tableName.lastIndexOf('.');
    if (lastPeriod > -1) {
      tableName = tableName.substring(lastPeriod + 1);
    }
    if (lowerCase) {
      tableName = tableName.toLowerCase();
    }
    return tableName;
  }

  /**
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  protected String trimQuotes(String tableName) {

    // remove quoted identifier characters
    for (int i = 0; i < quotedIdentifiers.length; i++) {
      tableName = tableName.replace(quotedIdentifiers[i], "");
    }
    return tableName;
  }

}
