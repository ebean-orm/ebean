package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.DbQuotes;

/**
 * Used to normalise table and column names which means stripping out
 * quoted identifier characters and any catalog or schema prefix.
 */
public class DdlNameNormalise {

  protected boolean lowerCaseTables = true;

  protected boolean lowerCaseColumns = true;

  protected DbQuotes quotes = new DbQuotes();

  public DdlNameNormalise() {
  }

  public boolean notQuoted(String tableName) {
    return quotes.notQuoted(tableName);
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
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  protected String trimQuotes(String tableName) {

    return quotes.trimQuotes(tableName);
  }

}
