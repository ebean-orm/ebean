package io.ebeaninternal.dbmigration.ddlgeneration;

/**
 * Options used when generated DDL.
 */
public class DdlOptions {

  private boolean foreignKeySkipCheck;

  public DdlOptions(boolean foreignKeySkipCheck) {
    this.foreignKeySkipCheck = foreignKeySkipCheck;
  }

  public DdlOptions() {
  }

  /**
   * Return true if ADD FOREIGN KEY should use a skip check option.
   */
  public boolean isForeignKeySkipCheck() {
    return foreignKeySkipCheck;
  }

  /**
   * Set to true if ADD FOREIGN KEY should use a skip check option.
   */
  public void setForeignKeySkipCheck(boolean foreignKeySkipCheck) {
    this.foreignKeySkipCheck = foreignKeySkipCheck;
  }

}
