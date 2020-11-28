package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import io.ebeaninternal.dbmigration.migration.Column;

import java.util.ArrayList;
import java.util.List;

class WriteUniqueConstraint {

  private final List<Column> primaryKeys = new ArrayList<>();
  private final List<Column> uniqueKeys = new ArrayList<>();

  WriteUniqueConstraint(List<Column> columns) {

    // filter for unique and primary keys
    for (Column column : columns) {
      if (Boolean.TRUE.equals(column.isPrimaryKey())) {
        primaryKeys.add(column);
      }
      if (hasValue(column.getUnique()) || hasValue(column.getUniqueOneToOne())){
        uniqueKeys.add(column);
      }
    }
  }

  /**
   * Return true if null or trimmed string is empty.
   */
  boolean hasValue(String value) {
    return value != null && !value.trim().isEmpty();
  }

  /**
   * Return the single columns with unique constraints (that are not the primary key).
   */
  public List<Column> uniqueKeys() {
    if (uniqueKeys.isEmpty() || primaryKeys.size() > 1) {
      // all single column unique constraints are valid
      return uniqueKeys;
    }
    // filter out PFK
    for (Column primaryKey : primaryKeys) {
      uniqueKeys.remove(primaryKey);
    }
    return uniqueKeys;
  }
}
