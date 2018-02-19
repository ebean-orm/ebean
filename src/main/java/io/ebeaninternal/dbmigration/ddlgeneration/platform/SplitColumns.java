package io.ebeaninternal.dbmigration.ddlgeneration.platform;

public class SplitColumns {

  /**
   * Return as an array of string column names.
   */
  public static String[] split(String columns) {
    return columns.split(",");
  }

}
