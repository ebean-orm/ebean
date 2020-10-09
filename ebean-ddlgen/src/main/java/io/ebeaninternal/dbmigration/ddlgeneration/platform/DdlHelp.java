package io.ebeaninternal.dbmigration.ddlgeneration.platform;

public class DdlHelp {
  public static final String DROP_DEFAULT = "DROP DEFAULT";

  public static final String DROP_COMMENT = "DROP COMMENT";

  public static final String DROP_CONSTRAINT = "DROP CONSTRAINT";

  public static final String DROP_FOREIGN_KEY = "DROP FOREIGN KEY";

  /**
   * Return true if the default value is the special DROP DEFAULT value.
   */
  public static boolean isDropDefault(String value) {
    return DROP_DEFAULT.equals(value);
  }

  /**
   * Return true if the default value is the special DROP COMMENT value.
   */
  public static boolean isDropComment(String value) {
    return DROP_COMMENT.equals(value);
  }
  
  /**
   * Return true if the default value is the special DROP CONSTRAINT value.
   */
  public static boolean isDropConstraint(String value) {
    return DROP_CONSTRAINT.equals(value);
  }

  /**
   * Return true if the default value is the special DROP FOREIGN KEY value.
   */
  public static boolean isDropForeignKey(String value) {
    return DROP_FOREIGN_KEY.equals(value);
  }
}
