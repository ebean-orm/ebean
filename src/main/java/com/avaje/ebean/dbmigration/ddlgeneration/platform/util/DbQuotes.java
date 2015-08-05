package com.avaje.ebean.dbmigration.ddlgeneration.platform.util;

/**
 * Used to normalise table and column names which means stripping out
 * quoted identifier characters and any catalog or schema prefix.
 */
public class DbQuotes {

  private final String[] quotedIdentifiers;

  public DbQuotes() {
    this.quotedIdentifiers = new String[]{"\"", "'", "[", "]", "`"};
  }

  public DbQuotes(String[] quotedIdentifiers) {
    this.quotedIdentifiers = quotedIdentifiers;
  }

  /**
   * Trim off the platform quoted identifier quotes like [ ' and ".
   */
  public boolean notQuoted(String tableName) {

    // remove quoted identifier characters
    for (int i = 0; i < quotedIdentifiers.length; i++) {
      if (tableName.contains(quotedIdentifiers[i])){
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
