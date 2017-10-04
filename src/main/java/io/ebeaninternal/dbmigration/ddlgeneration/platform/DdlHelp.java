package io.ebeaninternal.dbmigration.ddlgeneration.platform;

public class DdlHelp {
  public static final String DROP_DEFAULT = "DROP DEFAULT";

  public static final String DROP_COMMENT = "DROP COMMENT";

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

}
