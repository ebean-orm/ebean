package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.VowelRemover;

/**
 * Default implementation used to truncate or shorten db constraint names as required.
 */
public class DefaultConstraintMaxLength implements DbConstraintNaming.MaxLength {

  private final int maxConstraintNameLength;

  public DefaultConstraintMaxLength(int maxConstraintNameLength) {
    this.maxConstraintNameLength = maxConstraintNameLength;
  }

  /**
   * Apply a maximum length to the constraint name.
   * <p>
   * This implementation should work well apart from perhaps DB2 where the limit is 18.
   */
  public String maxLength(String constraintName, int count) {
    if (constraintName.length() < maxConstraintNameLength) {
      return constraintName;
    }
    if (maxConstraintNameLength < 60) {
      // trim out vowels for Oracle / DB2 with short max lengths
      constraintName = VowelRemover.trim(constraintName, 4);
      if (constraintName.length() < maxConstraintNameLength) {
        return constraintName;
      }
    }
    // add the count to ensure the constraint name is unique
    // (relying on the prefix having the table name to be globally unique)
    return constraintName.substring(0, maxConstraintNameLength - 3) + "_" + count;
  }
}
