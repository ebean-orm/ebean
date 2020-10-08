package io.ebeaninternal.dbmigration.ddlgeneration.platform;

public class SplitColumns {

  /**
   * Return as an array of string column names.
   */
  public static String[] split(String columns) {
    if (columns == null || columns.isEmpty()) {
      return new String[0];
    }
    return columns.split(",");
  }

  /**
   * Split returning null when columns is null or empty.
   */
  public static String[] splitWithNull(String columns) {
    if (columns == null || columns.isEmpty()) {
      return null;
    }
    return columns.split(",");
  }
}
