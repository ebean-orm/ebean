package io.ebeaninternal.dbmigration.model;

import io.ebean.annotation.ConstraintMode;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.DdlScript;
import io.ebeaninternal.server.deploy.DbMigrationInfo;

import java.util.List;
import java.util.Objects;

/**
 * A column in the logical model.
 */
public class MColumn {

  private static final String LOCALDATETIME = "localdatetime";
  private static final String TIMESTAMP = "timestamp";
  private String name;
  private String type;
  private String checkConstraint;
  private String checkConstraintName;
  private String defaultValue;
  private String references;
  private String foreignKeyName;
  private String foreignKeyIndex;
  private ConstraintMode fkeyOnDelete;
  private ConstraintMode fkeyOnUpdate;
  private String comment;

  private boolean historyExclude;
  private boolean notnull;
  private boolean primaryKey;
  private boolean identity;

  private String unique;

  /**
   * Special unique for OneToOne as we need to handle that different
   * specifically for MsSqlServer.
   */
  private String uniqueOneToOne;

  /**
   * Temporary variable used when building the alter column changes.
   */
  private AlterColumn alterColumn;

  private boolean draftOnly;

  private List<DbMigrationInfo> dbMigrationInfos;

  public MColumn(Column column) {
    this.name = column.getName();
    this.type = column.getType();
    this.checkConstraint = column.getCheckConstraint();
    this.checkConstraintName = column.getCheckConstraintName();
    this.defaultValue = column.getDefaultValue();
    this.comment = column.getComment();
    this.references = column.getReferences();
    this.foreignKeyName = column.getForeignKeyName();
    this.foreignKeyIndex = column.getForeignKeyIndex();
    this.fkeyOnDelete = fkeyMode(column.getForeignKeyOnDelete());
    this.fkeyOnUpdate = fkeyMode(column.getForeignKeyOnUpdate());
    this.notnull = Boolean.TRUE.equals(column.isNotnull());
    this.primaryKey = Boolean.TRUE.equals(column.isPrimaryKey());
    this.identity = Boolean.TRUE.equals(column.isIdentity());
    this.unique = column.getUnique();
    this.uniqueOneToOne = column.getUniqueOneToOne();
    this.historyExclude = Boolean.TRUE.equals(column.isHistoryExclude());
  }

  private ConstraintMode fkeyMode(String mode) {
    return (mode == null) ? null : ConstraintMode.valueOf(mode);
  }

  public MColumn(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public MColumn(String name, String type, boolean notnull) {
    this.name = name;
    this.type = type;
    this.notnull = notnull;
  }

  /**
   * Return a copy of this column used for creating the associated draft table.
   */
  public MColumn copyForDraft() {
    MColumn copy = new MColumn(name, type);
    copy.draftOnly = draftOnly;
    copy.checkConstraint = checkConstraint;
    copy.checkConstraintName = checkConstraintName;
    copy.defaultValue = defaultValue;
    copy.dbMigrationInfos = dbMigrationInfos;
    copy.references = references;
    copy.comment = comment;
    copy.foreignKeyName = foreignKeyName;
    copy.foreignKeyIndex = foreignKeyIndex;
    copy.fkeyOnUpdate = fkeyOnUpdate;
    copy.fkeyOnDelete = fkeyOnDelete;
    copy.historyExclude = historyExclude;
    copy.notnull = notnull;
    copy.primaryKey = primaryKey;
    copy.identity = identity;
    copy.unique = unique;
    copy.uniqueOneToOne = uniqueOneToOne;
    return copy;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean isIdentity() {
    return identity;
  }

  public void setIdentity(boolean identity) {
    this.identity = identity;
  }

  public String getCheckConstraint() {
    return checkConstraint;
  }

  public void setCheckConstraint(String checkConstraint) {
    this.checkConstraint = checkConstraint;
  }

  public String getCheckConstraintName() {
    return checkConstraintName;
  }

  public void setCheckConstraintName(String checkConstraintName) {
    this.checkConstraintName = checkConstraintName;
  }

  public String getForeignKeyName() {
    return foreignKeyName;
  }

  public void setForeignKeyName(String foreignKeyName) {
    this.foreignKeyName = foreignKeyName;
  }

  public String getForeignKeyIndex() {
    return foreignKeyIndex;
  }

  public void setForeignKeyIndex(String foreignKeyIndex) {
    this.foreignKeyIndex = foreignKeyIndex;
  }

  public void setForeignKeyModes(ConstraintMode onDelete, ConstraintMode onUpdate) {
    this.fkeyOnDelete = onDelete;
    this.fkeyOnUpdate = onUpdate;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String getReferences() {
    return references;
  }

  public void setReferences(String references) {
    this.references = references;
  }

  public boolean isNotnull() {
    return notnull;
  }

  public void setNotnull(boolean notnull) {
    this.notnull = notnull;
  }

  public boolean isHistoryExclude() {
    return historyExclude;
  }

  public void setHistoryExclude(boolean historyExclude) {
    this.historyExclude = historyExclude;
  }

  public void setUnique(String unique) {
    this.unique = unique;
  }

  public String getUnique() {
    return unique;
  }

  /**
   * Set unique specifically for OneToOne mapping.
   * We need special DDL for this case for SqlServer.
   */
  public void setUniqueOneToOne(String uniqueOneToOne) {
    this.uniqueOneToOne = uniqueOneToOne;
  }

  /**
   * Return true if this is unique for a OneToOne.
   */
  public String getUniqueOneToOne() {
    return uniqueOneToOne;
  }

  /**
   * Return the column comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Set the column comment.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Set the draftOnly status for this column.
   */
  public void setDraftOnly(boolean draftOnly) {
    this.draftOnly = draftOnly;
  }

  /**
   * Return the draftOnly status for this column.
   */
  public boolean isDraftOnly() {
    return draftOnly;
  }

  /**
   * Return true if this column should be included in History DB triggers etc.
   */
  public boolean isIncludeInHistory() {
    return !draftOnly && !historyExclude;
  }

  public void clearForeignKey() {
    this.references = null;
    this.foreignKeyName = null;
    this.fkeyOnDelete = null;
    this.fkeyOnUpdate = null;
  }

  public Column createColumn() {

    Column c = new Column();
    c.setName(name);
    c.setType(type);

    if (notnull) c.setNotnull(true);
    if (primaryKey) c.setPrimaryKey(true);
    if (identity) c.setIdentity(true);
    if (historyExclude) c.setHistoryExclude(true);

    c.setCheckConstraint(checkConstraint);
    c.setCheckConstraintName(checkConstraintName);
    c.setReferences(references);
    c.setForeignKeyName(foreignKeyName);
    c.setForeignKeyIndex(foreignKeyIndex);
    c.setForeignKeyOnDelete(fkeyModeOf(fkeyOnDelete));
    c.setForeignKeyOnUpdate(fkeyModeOf(fkeyOnUpdate));
    c.setDefaultValue(defaultValue);
    c.setComment(comment);
    c.setUnique(unique);
    c.setUniqueOneToOne(uniqueOneToOne);

    if (dbMigrationInfos != null) {
      for (DbMigrationInfo info : dbMigrationInfos) {
        if (!info.getPreAdd().isEmpty()) {
          DdlScript script = new DdlScript();
          script.getDdl().addAll(info.getPreAdd());
          script.setPlatforms(info.joinPlatforms());
          c.getBefore().add(script);
        }

        if (!info.getPostAdd().isEmpty()) {
          DdlScript script = new DdlScript();
          script.getDdl().addAll(info.getPostAdd());
          script.setPlatforms(info.joinPlatforms());
          c.getAfter().add(script);
        }
      }
    }

    return c;
  }

  private String fkeyModeOf(ConstraintMode mode) {
    return (mode == null) ? null : mode.name();
  }

  protected static boolean different(String val1, String val2) {
    return !Objects.equals(val1, val2);
  }

  private boolean hasValue(String val) {
    return val != null && !val.isEmpty();
  }

  private boolean hasValue(Boolean val) {
    return val != null;
  }

  private AlterColumn getAlterColumn(String tableName, boolean tableWithHistory) {
    if (alterColumn == null) {
      alterColumn = new AlterColumn();
      alterColumn.setColumnName(name);
      alterColumn.setTableName(tableName);
      if (tableWithHistory) {
        alterColumn.setWithHistory(Boolean.TRUE);
      }

      if (dbMigrationInfos != null) {
        for (DbMigrationInfo info : dbMigrationInfos) {
          if (!info.getPreAlter().isEmpty()) {
            DdlScript script = new DdlScript();
            script.getDdl().addAll(info.getPreAlter());
            script.setPlatforms(info.joinPlatforms());
            alterColumn.getBefore().add(script);
          }

          if (!info.getPostAlter().isEmpty()) {
            DdlScript script = new DdlScript();
            script.getDdl().addAll(info.getPostAlter());
            script.setPlatforms(info.joinPlatforms());
            alterColumn.getAfter().add(script);
          }
        }
      }
    }
    return alterColumn;
  }

  /**
   * Compare the column meta data and return true if there is a change that means
   * the history table column needs
   */
  public void compare(ModelDiff modelDiff, MTable table, MColumn newColumn) {

    this.dbMigrationInfos = newColumn.dbMigrationInfos;

    boolean tableWithHistory = table.isWithHistory();
    String tableName = table.getName();

    // set to null and check at the end
    this.alterColumn = null;

    boolean changeBaseAttribute = false;

    if (historyExclude != newColumn.historyExclude) {
      getAlterColumn(tableName, tableWithHistory).setHistoryExclude(newColumn.historyExclude);
    }

    if (different(type, newColumn.type) && !localDateTime(type, newColumn.type)) {
      changeBaseAttribute = true;
      getAlterColumn(tableName, tableWithHistory).setType(newColumn.type);
    }
    if (notnull != newColumn.notnull) {
      changeBaseAttribute = true;
      getAlterColumn(tableName, tableWithHistory).setNotnull(newColumn.notnull);
    }
    if (different(defaultValue, newColumn.defaultValue)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (newColumn.defaultValue == null) {
        alter.setDefaultValue(DdlHelp.DROP_DEFAULT);
      } else {
        alter.setDefaultValue(newColumn.defaultValue);
      }
    }
    if (different(comment, newColumn.comment)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (newColumn.comment == null) {
        alter.setComment(DdlHelp.DROP_COMMENT);
      } else {
        alter.setComment(newColumn.comment);
      }
    }
    if (different(checkConstraint, newColumn.checkConstraint)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(checkConstraint) && !hasValue(newColumn.checkConstraint)) {
        alter.setDropCheckConstraint(checkConstraintName);
      }
      if (hasValue(newColumn.checkConstraint)) {
        alter.setCheckConstraintName(newColumn.checkConstraintName);
        alter.setCheckConstraint(newColumn.checkConstraint);
      }
    }
    if (different(references, newColumn.references)
        || hasValue(newColumn.references) && fkeyOnDelete != newColumn.fkeyOnDelete
        || hasValue(newColumn.references) && fkeyOnUpdate != newColumn.fkeyOnUpdate) {
      // foreign key change
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(foreignKeyName)) {
        alter.setDropForeignKey(foreignKeyName);
      }
      if (hasValue(foreignKeyIndex)) {
        alter.setDropForeignKeyIndex(foreignKeyIndex);
      }
      if (hasValue(newColumn.references)) {
        // add new foreign key constraint
        alter.setReferences(newColumn.references);
        alter.setForeignKeyName(newColumn.foreignKeyName);
        alter.setForeignKeyIndex(newColumn.foreignKeyIndex);
        if (newColumn.fkeyOnDelete != null) {
          alter.setForeignKeyOnDelete(fkeyModeOf(newColumn.fkeyOnDelete));
        }
        if (newColumn.fkeyOnUpdate != null) {
          alter.setForeignKeyOnUpdate(fkeyModeOf(newColumn.fkeyOnUpdate));
        }
      }
    }

    if (different(unique, newColumn.unique)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(unique)) {
        alter.setDropUnique(unique);
      }
      if (hasValue(newColumn.unique)) {
        alter.setUnique(newColumn.unique);
      }
    }
    if (different(uniqueOneToOne, newColumn.uniqueOneToOne)) {
      AlterColumn alter = getAlterColumn(tableName, tableWithHistory);
      if (hasValue(uniqueOneToOne)) {
        alter.setDropUnique(uniqueOneToOne);
      }
      if (hasValue(newColumn.uniqueOneToOne)) {
        alter.setUniqueOneToOne(newColumn.uniqueOneToOne);
      }
    }

    if (alterColumn != null) {
      modelDiff.addAlterColumn(alterColumn);
      if (changeBaseAttribute) {
        // support reverting these changes
        alterColumn.setCurrentType(type);
        alterColumn.setCurrentNotnull(notnull);
        alterColumn.setCurrentDefaultValue(defaultValue);
      }
    }
  }

  /**
   * Ignore the case of new type LocalDateTime which was historically mapped to Timestamp.
   */
  boolean localDateTime(String type, String newType) {
    return LOCALDATETIME.equalsIgnoreCase(newType) && TIMESTAMP.equalsIgnoreCase(type);
  }

  public void setDbMigrationInfos(List<DbMigrationInfo> dbMigrationInfos) {
    this.dbMigrationInfos = dbMigrationInfos;
  }

  /**
   * Rename the column.
   */
  public MColumn rename(String newName) {
    this.name = newName;
    return this;
  }

  /**
   * Apply changes based on the AlterColumn request.
   */
  public void apply(AlterColumn alterColumn) {

    if (hasValue(alterColumn.getDropCheckConstraint())) {
      checkConstraint = null;
    }
    if (hasValue(alterColumn.getDropForeignKey())) {
      foreignKeyName = null;
      references = null;
    }
    if (hasValue(alterColumn.getDropForeignKeyIndex())) {
      foreignKeyIndex = null;
    }
    if (hasValue(alterColumn.getDropUnique())) {
      unique = null;
      uniqueOneToOne = null;
    }

    if (hasValue(alterColumn.getType())) {
      type = alterColumn.getType();
    }
    if (hasValue(alterColumn.isNotnull())) {
      notnull = alterColumn.isNotnull();
    }
    if (hasValue(alterColumn.getDefaultValue())) {
      defaultValue = alterColumn.getDefaultValue();
      if (DdlHelp.isDropDefault(defaultValue)) {
        defaultValue = null;
      }
    }
    if (hasValue(alterColumn.getCheckConstraint())) {
      checkConstraint = alterColumn.getCheckConstraint();
    }
    if (hasValue(alterColumn.getCheckConstraintName())) {
      checkConstraintName = alterColumn.getCheckConstraintName();
    }
    if (hasValue(alterColumn.getUnique())) {
      unique = alterColumn.getUnique();
    }
    if (hasValue(alterColumn.getUniqueOneToOne())) {
      uniqueOneToOne = alterColumn.getUniqueOneToOne();
    }
    if (hasValue(alterColumn.getReferences())) {
      references = alterColumn.getReferences();
    }
    if (hasValue(alterColumn.getForeignKeyName())) {
      foreignKeyName = alterColumn.getForeignKeyName();
    }
    if (hasValue(alterColumn.getForeignKeyIndex())) {
      foreignKeyIndex = alterColumn.getForeignKeyIndex();
    }
    if (hasValue(alterColumn.getComment())) {
      comment = alterColumn.getComment();
      if (DdlHelp.isDropComment(comment)) {
        comment = null;
      }
    }
    if (hasValue(alterColumn.getForeignKeyOnDelete())) {
      fkeyOnDelete = fkeyMode(alterColumn.getForeignKeyOnDelete());
    }
    if (hasValue(alterColumn.getForeignKeyOnUpdate())) {
      fkeyOnUpdate = fkeyMode(alterColumn.getForeignKeyOnUpdate());
    }
    if (hasValue(alterColumn.isHistoryExclude())) {
      historyExclude = alterColumn.isHistoryExclude();
    }
  }
}
