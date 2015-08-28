package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.AddHistoryTable;
import com.avaje.ebean.dbmigration.migration.AlterColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.DropHistoryTable;
import com.avaje.ebean.dbmigration.migration.DropTable;
import com.avaje.ebean.dbmigration.migration.IdentityType;
import com.avaje.ebean.dbmigration.migration.UniqueConstraint;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the logical model for a given Table and everything associated to it.
 * <p>
 * This effectively represents a table, its columns and all associated
 * constraints, foreign keys and indexes.
 * </p>
 * <p>
 * Migrations can be applied to this such that it represents the state
 * of a given table after various migrations have been applied.
 * </p>
 * <p>
 * This table model can also be derived from the EbeanServer bean descriptor
 * and associated properties.
 * </p>
 */
public class MTable {

  /**
   * Table name.
   */
  private final String name;

  /**
   * Primary key name.
   */
  private String pkName;

  /**
   * Table comment.
   */
  private String comment;

  /**
   * Tablespace to use.
   */
  private String tablespace;

  /**
   * Tablespace to use for indexes on this table.
   */
  private String indexTablespace;

  /**
   * If set then this overrides the platform default so for UUID generated values
   * or DB's supporting both sequences and autoincrement.
   */
  private IdentityType identityType;

  /**
   * DB sequence name.
   */
  private String sequenceName;
  private int sequenceInitial;
  private int sequenceAllocate;

  /**
   * If set to true this table should has history support.
   */
  private boolean withHistory;

  /**
   * The columns on the table.
   */
  private Map<String, MColumn> columns = new LinkedHashMap<String, MColumn>();

  /**
   * Compound unique constraints.
   */
  private List<MCompoundUniqueConstraint> compoundUniqueConstraints = new ArrayList<MCompoundUniqueConstraint>();

  /**
   * Compound foreign keys.
   */
  private List<MCompoundForeignKey> compoundKeys = new ArrayList<MCompoundForeignKey>();

  /**
   * Column name for the 'When created' column. This can be used for the initial effective start date when adding
   * history to an existing table and maps to a @WhenCreated or @CreatedTimestamp column.
   */
  private String whenCreatedColumn;

  /**
   * Temporary - holds addColumn settings.
   */
  private AddColumn addColumn;


  /**
   * Construct for migration.
   */
  public MTable(CreateTable createTable) {
    this.name = createTable.getName();
    this.pkName = createTable.getPkName();
    this.comment = createTable.getComment();
    this.tablespace = createTable.getTablespace();
    this.indexTablespace = createTable.getIndexTablespace();
    this.withHistory = Boolean.TRUE.equals(createTable.isWithHistory());
    this.sequenceName = createTable.getSequenceName();
    this.sequenceInitial = toInt(createTable.getSequenceInitial());
    this.sequenceAllocate = toInt(createTable.getSequenceAllocate());
    List<Column> cols = createTable.getColumn();
    for (Column column : cols) {
      addColumn(column);
    }
  }


  /**
   * Construct typically from EbeanServer meta data.
   */
  public MTable(String name) {
    this.name = name;
  }

  /**
   * Return the DropTable migration for this table.
   */
  public DropTable dropTable() {
    DropTable dropTable = new DropTable();
    dropTable.setName(name);
    return dropTable;
  }

  /**
   * Return the CreateTable migration for this table.
   */
  public CreateTable createTable() {

    CreateTable createTable = new CreateTable();
    createTable.setName(name);
    createTable.setPkName(pkName);
    createTable.setComment(comment);
    createTable.setTablespace(tablespace);
    createTable.setIndexTablespace(indexTablespace);
    createTable.setSequenceName(sequenceName);
    createTable.setSequenceInitial(toBigInteger(sequenceInitial));
    createTable.setSequenceAllocate(toBigInteger(sequenceAllocate));
    createTable.setIdentityType(identityType);
    if (withHistory) {
      createTable.setWithHistory(Boolean.TRUE);
    }

    for (MColumn column : this.columns.values()) {
      createTable.getColumn().add(column.createColumn());
    }

    for (MCompoundForeignKey compoundKey : compoundKeys) {
      createTable.getForeignKey().add(compoundKey.createForeignKey());
    }

    for (MCompoundUniqueConstraint constraint : compoundUniqueConstraints) {
      UniqueConstraint uq = new UniqueConstraint();
      uq.setName(constraint.getName());
      String[] columns = constraint.getColumns();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < columns.length; i++) {
        if (i > 0) {
          sb.append(",");
        }
        sb.append(columns[i]);
      }
      uq.setColumnNames(sb.toString());
      createTable.getUniqueConstraint().add(uq);
    }

    return createTable;
  }

  /**
   * Compare to another version of the same table to perform a diff.
   */
  public void compare(ModelDiff modelDiff, MTable newTable) {

    if (withHistory != newTable.withHistory) {
      if (withHistory) {
        DropHistoryTable dropHistoryTable = new DropHistoryTable();
        dropHistoryTable.setBaseTable(name);
        modelDiff.addDropHistoryTable(dropHistoryTable);

      } else {
        AddHistoryTable addHistoryTable = new AddHistoryTable();
        addHistoryTable.setBaseTable(name);
        modelDiff.addAddHistoryTable(addHistoryTable);
      }
    }

    addColumn = null;

    Set<String> mappedColumns = new LinkedHashSet<String>();

    Collection<MColumn> newColumns = newTable.getColumns().values();
    for (MColumn newColumn : newColumns) {
      MColumn localColumn = columns.get(newColumn.getName());
      if (localColumn == null) {
        diffNewColumn(newColumn);
      } else {
        // note that if there are alter column changes in here then
        // the table withHistory is taken into account
        localColumn.compare(modelDiff, this, newColumn);
        mappedColumns.add(newColumn.getName());
      }
    }

    Collection<MColumn> existingColumns = columns.values();
    for (MColumn existingColumn : existingColumns) {
      if (!mappedColumns.contains(existingColumn.getName())) {
        diffDropColumn(modelDiff, existingColumn);
      }
    }

    if (addColumn != null) {
      modelDiff.addAddColumn(addColumn);
    }
  }

  /**
   * Apply AddColumn migration.
   */
  public void apply(AddColumn addColumn) {
    checkTableName(addColumn.getTableName());
    for (Column column : addColumn.getColumn()) {
      addColumn(column);
    }
  }

  /**
   * Apply AddColumn migration.
   */
  public void apply(AlterColumn alterColumn) {
    checkTableName(alterColumn.getTableName());
    String columnName = alterColumn.getColumnName();
    MColumn existingColumn = columns.get(columnName);
    if (existingColumn == null) {
      throw new IllegalStateException("Column [" + columnName + "] does not exist for AlterColumn change?");
    }
    existingColumn.apply(alterColumn);
  }

  /**
   * Apply DropColumn migration.
   */
  public void apply(DropColumn dropColumn) {
    checkTableName(dropColumn.getTableName());
    columns.remove(dropColumn.getColumnName());
  }

  public String getName() {
    return name;
  }

  public String getPkName() {
    return pkName;
  }

  public void setPkName(String pkName) {
    this.pkName = pkName;
  }

  public String getComment() {
    return comment;
  }

  public String getTablespace() {
    return tablespace;
  }

  public String getIndexTablespace() {
    return indexTablespace;
  }

  public boolean isWithHistory() {
    return withHistory;
  }

  public void setWithHistory(boolean withHistory) {
    this.withHistory = withHistory;
  }

  public Map<String, MColumn> getColumns() {
    return columns;
  }

  public List<MCompoundUniqueConstraint> getCompoundUniqueConstraints() {
    return compoundUniqueConstraints;
  }

  public List<MCompoundForeignKey> getCompoundKeys() {
    return compoundKeys;
  }

  public String getSequenceName() {
    return sequenceName;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public int getSequenceInitial() {
    return sequenceInitial;
  }

  public void setSequenceInitial(int sequenceInitial) {
    this.sequenceInitial = sequenceInitial;
  }

  public int getSequenceAllocate() {
    return sequenceAllocate;
  }

  public void setSequenceAllocate(int sequenceAllocate) {
    this.sequenceAllocate = sequenceAllocate;
  }

  public void setWhenCreatedColumn(String whenCreatedColumn) {
    this.whenCreatedColumn = whenCreatedColumn;
  }

  public String getWhenCreatedColumn() {
    return whenCreatedColumn;
  }

  /**
   * Set the identity type to use for this table.
   * <p>
   * If set then this overrides the platform default so for UUID generated values
   * or DB's supporting both sequences and autoincrement.
   */
  public void setIdentityType(IdentityType identityType) {
    this.identityType = identityType;
  }

  /**
   * Returns the identity type to use for this table.
   * <p>
   * If set then this overrides the platform default so for UUID generated values
   * or DB's supporting both sequences and autoincrement.
   */
  public IdentityType getIdentityType() {
    return identityType;
  }

  /**
   * Return the list of columns that make the primary key.
   */
  public List<MColumn> primaryKeyColumns() {
    List<MColumn> pk = new ArrayList<MColumn>(3);
    for (MColumn column : columns.values()) {
      if (column.isPrimaryKey()) {
        pk.add(column);
      }
    }
    return pk;
  }

  private void checkTableName(String tableName) {
    if (!name.equals(tableName)) {
      throw new IllegalArgumentException("addColumn tableName [" + tableName + "] does not match [" + name + "]");
    }
  }

  /**
   * Add a column via migration data.
   */
  private void addColumn(Column column) {
    columns.put(column.getName(), new MColumn(column));
  }

  /**
   * Add a model column (typically from EbeanServer meta data).
   */
  public void addColumn(MColumn column) {
    columns.put(column.getName(), column);
  }

  /**
   * Add a compound unique constraint.
   */
  public void addCompoundUniqueConstraint(String[] columns, boolean oneToOne, String constraintName) {
    compoundUniqueConstraints.add(new MCompoundUniqueConstraint(columns, oneToOne, constraintName));
  }

  /**
   * Add a compound unique constraint.
   */
  public void addCompoundUniqueConstraint(List<MColumn> columns, boolean oneToOne, String constraintName) {
    String[] cols = new String[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      cols[i] = columns.get(i).getName();
    }
    addCompoundUniqueConstraint(cols, oneToOne, constraintName);
  }

  /**
   * Add a compound foreign key.
   */
  public void addForeignKey(MCompoundForeignKey compoundKey) {
    compoundKeys.add(compoundKey);
  }

  /**
   * Add a column checking if it already exists and if so return the existing column.
   * Sometimes the case for a primaryKey that is also a foreign key.
   */
  public MColumn addColumn(String dbCol, String columnDefn, boolean notnull) {

    MColumn existingColumn = columns.get(dbCol);
    if (existingColumn != null) {
      if (notnull) {
        existingColumn.setNotnull(true);
      }
      return existingColumn;
    }

    MColumn newCol = new MColumn(dbCol, columnDefn, notnull);
    addColumn(newCol);
    return newCol;
  }

  /**
   * Add a 'new column' to the AddColumn migration object.
   */
  private void diffNewColumn(MColumn newColumn) {

    if (addColumn == null) {
      addColumn = new AddColumn();
      addColumn.setTableName(name);
      if (withHistory) {
        // These addColumns need to occur on the history
        // table as well as the base table
        addColumn.setWithHistory(Boolean.TRUE);
      }
    }

    addColumn.getColumn().add(newColumn.createColumn());
  }

  /**
   * Add a 'drop column' to the diff.
   */
  private void diffDropColumn(ModelDiff modelDiff, MColumn existingColumn) {

    DropColumn dropColumn = new DropColumn();
    dropColumn.setTableName(name);
    dropColumn.setColumnName(existingColumn.getName());
    if (withHistory) {
      // These dropColumns should occur on the history
      // table as well as the base table
      dropColumn.setWithHistory(Boolean.TRUE);
    }

    modelDiff.addDropColumn(dropColumn);
  }

  private int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }

  private BigInteger toBigInteger(int value) {
    return (value == 0) ? null : BigInteger.valueOf(value);
  }

}
