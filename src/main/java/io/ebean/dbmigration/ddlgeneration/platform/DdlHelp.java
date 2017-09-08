package io.ebean.dbmigration.ddlgeneration.platform;

public class DdlHelp {
  public static final String DROP_DEFAULT = "DROP DEFAULT";
  
  public static final String DROP_COMMENT = "DROP COMMENT";
  
  public static final String DROP_CONSTRAINT = "DROP CONSTRAINT";
  
  /**
   * Return true if the default value is the special DROP DEFAULT value.
   */
  public static boolean isDropDefault(String defaultValue) {
    return DROP_DEFAULT.equals(defaultValue);
  }
  
  /**
   * Return true if the default value is the special DROP DEFAULT value.
   */
  public static boolean isDropComment(String comment) {
    return DROP_COMMENT.equals(comment);
  }
  
  /**
   * Return true if the default value is the special DROP CONSTRAINT value.
   */
  public static boolean isDropConstraint(String comment) {
    return DROP_CONSTRAINT.equals(comment);
  }
}
