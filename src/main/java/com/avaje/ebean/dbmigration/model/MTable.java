package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.Column;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.IdentityType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
   * Flag set to indicate
   */
  private boolean matched;

  private final String name;

  private String comment;

  private String tablespace;

  private String indexTablespace;

  /**
   * If set then this overrides the platform default so for UUID generated values
   * or DB's supporting both sequences and autoincrement.
   */
  private IdentityType identityType;

  private String sequenceName;
  private int sequenceInitial;
  private int sequenceAllocate;

  private Boolean withHistory;

  private Map<String, MColumn> columns = new LinkedHashMap<String, MColumn>();

  private List<MCompoundUniqueConstraint> compoundUniqueConstraints = new ArrayList<MCompoundUniqueConstraint>();

  private List<MCompoundForeignKey> compoundKeys = new ArrayList<MCompoundForeignKey>();

  /**
   * Construct for migration.
   */
  public MTable(CreateTable createTable) {
    this.name = createTable.getName();
    this.comment = createTable.getComment();
    this.tablespace = createTable.getTablespace();
    this.indexTablespace = createTable.getIndexTablespace();
    this.withHistory = createTable.isWithHistory();
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

  public CreateTable createTable() {

    CreateTable createTable = new CreateTable();
    createTable.setName(name);
    createTable.setComment(comment);
    createTable.setTablespace(tablespace);
    createTable.setIndexTablespace(indexTablespace);
    createTable.setWithHistory(withHistory);
    createTable.setSequenceName(sequenceName);
    createTable.setSequenceInitial(toBigInteger(sequenceInitial));
    createTable.setSequenceAllocate(toBigInteger(sequenceAllocate));
    createTable.setIdentityType(identityType);

    for (MColumn column : this.columns.values()) {
      createTable.getColumn().add(column.createColumn());
    }

    for (MCompoundForeignKey compoundKey : compoundKeys) {
      createTable.getForeignKey().add(compoundKey.createForeignKey());
    }

    return createTable;
  }

  public boolean isMatched() {
    return matched;
  }

  public void setMatched(boolean matched) {
    this.matched = matched;
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
   * Apply DropColumn migration.
   */
  public void apply(DropColumn dropColumn) {
    checkTableName(dropColumn.getTableName());
    columns.remove(dropColumn.getColumnName());
  }

  public String getName() {
    return name;
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

  public Boolean getWithHistory() {
    return withHistory;
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
  public void addCompoundUniqueConstraint(String[] columns, boolean oneToOne) {
    compoundUniqueConstraints.add(new MCompoundUniqueConstraint(columns, oneToOne));
  }

  /**
   * Add a compound unique constraint.
   */
  public void addCompoundUniqueConstraint(List<MColumn> columns, boolean oneToOne) {
    String[] cols = new String[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      cols[i] = columns.get(i).getName();
    }
    addCompoundUniqueConstraint(cols, oneToOne);
  }

  public void addForeignKey(MCompoundForeignKey compoundKey) {
    compoundKeys.add(compoundKey);
  }

  private int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }

  private BigInteger toBigInteger(int value) {
    return (value == 0) ? null : BigInteger.valueOf(value);
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
}
