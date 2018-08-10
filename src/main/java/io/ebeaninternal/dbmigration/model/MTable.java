package io.ebeaninternal.dbmigration.model;

import io.ebean.annotation.PartitionMode;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.SplitColumns;
import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.Column;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropTable;
import io.ebeaninternal.dbmigration.migration.ForeignKey;
import io.ebeaninternal.dbmigration.migration.IdentityType;
import io.ebeaninternal.dbmigration.migration.UniqueConstraint;
import io.ebeaninternal.server.deploy.PartitionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

  private static final Logger logger = LoggerFactory.getLogger(MTable.class);

  /**
   * Table name.
   */
  private final String name;

  /**
   * The associated draft table.
   */
  private MTable draftTable;

  /**
   * Marked true for draft tables. These need to have their FK references adjusted
   * after all the draft tables have been identified.
   */
  private boolean draft;

  private PartitionMeta partitionMeta;

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
  private Map<String, MColumn> columns = new LinkedHashMap<>();

  /**
   * Compound unique constraints.
   */
  private List<MCompoundUniqueConstraint> uniqueConstraints = new ArrayList<>();

  /**
   * Compound foreign keys.
   */
  private List<MCompoundForeignKey> compoundKeys = new ArrayList<>();

  /**
   * Column name for the 'When created' column. This can be used for the initial effective start date when adding
   * history to an existing table and maps to a @WhenCreated or @CreatedTimestamp column.
   */
  private String whenCreatedColumn;

  /**
   * Temporary - holds addColumn settings.
   */
  private AddColumn addColumn;

  private List<String> droppedColumns = new ArrayList<>();

  /**
   * Create a copy of this table structure as a 'draft' table.
   * <p>
   * Note that both tables contain @DraftOnly MColumns and these are filtered out
   * later when creating the CreateTable object.
   */
  public MTable createDraftTable() {

    draftTable = new MTable(name + "_draft");
    draftTable.draft = true;
    draftTable.whenCreatedColumn = whenCreatedColumn;
    // compoundKeys
    // compoundUniqueConstraints
    draftTable.identityType = identityType;

    for (MColumn col : allColumns()) {
      draftTable.addColumn(col.copyForDraft());
    }

    return draftTable;
  }

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
    this.draft = Boolean.TRUE.equals(createTable.isDraft());
    this.sequenceName = createTable.getSequenceName();
    this.sequenceInitial = toInt(createTable.getSequenceInitial());
    this.sequenceAllocate = toInt(createTable.getSequenceAllocate());
    List<Column> cols = createTable.getColumn();
    for (Column column : cols) {
      addColumn(column);
    }
    List<UniqueConstraint> uqConstraints = createTable.getUniqueConstraint();
    for (UniqueConstraint uq : uqConstraints) {
      MCompoundUniqueConstraint mUq = new MCompoundUniqueConstraint(SplitColumns.split(uq.getColumnNames()), uq.isOneToOne(), uq.getName());
      mUq.setNullableColumns(SplitColumns.split(uq.getNullableColumns()));
      uniqueConstraints.add(mUq);
    }

    for (ForeignKey fk : createTable.getForeignKey()) {
      if (DdlHelp.isDropForeignKey(fk.getColumnNames())) {
        removeForeignKey(fk.getName());
      } else {
        addForeignKey(fk.getName(), fk.getRefTableName(), fk.getIndexName(), fk.getColumnNames(), fk.getRefColumnNames());
      }
    }
  }


  public void addForeignKey(String name, String refTableName, String indexName, String columnNames, String refColumnNames) {
    MCompoundForeignKey foreignKey = new MCompoundForeignKey(name, refTableName, indexName);
    String[] cols = SplitColumns.split(columnNames);
    String[] refCols = SplitColumns.split(refColumnNames);
    for (int i = 0; i < cols.length && i < refCols.length; i++) {
      foreignKey.addColumnPair(cols[i], refCols[i]);
    }
    addForeignKey(foreignKey);
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
    // we must add pk col name & sequence name, as we have to delete the sequence also.
    if (identityType != IdentityType.GENERATOR && identityType != IdentityType.EXTERNAL) {
      String pkCol = null;
      for (MColumn column : columns.values()) {
        if (column.isPrimaryKey()) {
          if (pkCol == null) {
            pkCol = column.getName();
          } else { // multiple pk cols -> no sequence
            pkCol = null;
            break;
          }
        }
      }
      if (pkCol != null) {
        dropTable.setSequenceCol(pkCol);
        dropTable.setSequenceName(sequenceName);
      }
    }
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
    if (partitionMeta != null) {
      createTable.setPartitionMode(partitionMeta.getMode().name());
      createTable.setPartitionColumn(partitionMeta.getProperty());
    }
    createTable.setTablespace(tablespace);
    createTable.setIndexTablespace(indexTablespace);
    createTable.setSequenceName(sequenceName);
    createTable.setSequenceInitial(toBigInteger(sequenceInitial));
    createTable.setSequenceAllocate(toBigInteger(sequenceAllocate));
    createTable.setIdentityType(identityType);
    if (withHistory) {
      createTable.setWithHistory(Boolean.TRUE);
    }
    if (draft) {
      createTable.setDraft(Boolean.TRUE);
    }

    for (MColumn column : allColumns()) {
      // filter out draftOnly columns from the base table
      if (draft || !column.isDraftOnly()) {
        createTable.getColumn().add(column.createColumn());
      }
    }

    for (MCompoundForeignKey compoundKey : compoundKeys) {
      createTable.getForeignKey().add(compoundKey.createForeignKey());
    }

    for (MCompoundUniqueConstraint constraint : uniqueConstraints) {
      UniqueConstraint uq = constraint.getUniqueConstraint();
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

    compareColumns(modelDiff, newTable);

    if (MColumn.different(comment, newTable.comment)) {
      AddTableComment addTableComment = new AddTableComment();
      addTableComment.setName(name);
      if (newTable.comment == null) {
        addTableComment.setComment(DdlHelp.DROP_COMMENT);
      } else {
        addTableComment.setComment(newTable.comment);
      }
      modelDiff.addTableComment(addTableComment);
    }


    compareCompoundKeys(modelDiff, newTable);
    compareUniqueKeys(modelDiff, newTable);
  }

  private void compareColumns(ModelDiff modelDiff, MTable newTable) {
    addColumn = null;

    Map<String, MColumn> newColumnMap = newTable.getColumns();

    // compare newColumns to existing columns (look for new and diff columns)
    for (MColumn newColumn : newColumnMap.values()) {
      MColumn localColumn = getColumn(newColumn.getName());
      if (localColumn == null) {
        // can ignore if draftOnly column and non-draft table
        if (!newColumn.isDraftOnly() || draft) {
          diffNewColumn(newColumn);
        }
      } else {
        localColumn.compare(modelDiff, this, newColumn);
      }
    }

    // compare existing columns (look for dropped columns)
    for (MColumn existingColumn : allColumns()) {
      MColumn newColumn = newColumnMap.get(existingColumn.getName());
      if (newColumn == null) {
        diffDropColumn(modelDiff, existingColumn);
      } else if (newColumn.isDraftOnly() && !draft) {
        // effectively a drop column (draft only column on a non-draft table)
        logger.trace("... drop column {} from table {} as now draftOnly", newColumn.getName(), name);
        diffDropColumn(modelDiff, existingColumn);
      }
    }

    if (addColumn != null) {
      modelDiff.addAddColumn(addColumn);
    }
  }


  private void compareCompoundKeys(ModelDiff modelDiff, MTable newTable) {
    List<MCompoundForeignKey> newKeys = new ArrayList<>(newTable.getCompoundKeys());
    List<MCompoundForeignKey> currentKeys = new ArrayList<>(getCompoundKeys());

    // remove keys that have not changed
    currentKeys.removeAll(newTable.getCompoundKeys());
    newKeys.removeAll(getCompoundKeys());

    for (MCompoundForeignKey currentKey : currentKeys) {
      modelDiff.addAlterForeignKey(currentKey.dropForeignKey(name));
    }

    for (MCompoundForeignKey newKey : newKeys) {
      modelDiff.addAlterForeignKey(newKey.addForeignKey(name));
    }
  }

  private void compareUniqueKeys(ModelDiff modelDiff, MTable newTable) {
    List<MCompoundUniqueConstraint> newKeys = new ArrayList<>(newTable.getUniqueConstraints());
    List<MCompoundUniqueConstraint> currentKeys = new ArrayList<>(getUniqueConstraints());

    // remove keys that have not changed
    currentKeys.removeAll(newTable.getUniqueConstraints());
    newKeys.removeAll(getUniqueConstraints());

    for (MCompoundUniqueConstraint currentKey: currentKeys) {
      modelDiff.addUniqueConstraint(currentKey.dropUniqueConstraint(name));
    }
    for (MCompoundUniqueConstraint newKey: newKeys) {
      modelDiff.addUniqueConstraint(newKey.addUniqueConstraint(name));
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
    MColumn existingColumn = getColumn(columnName);
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
    MColumn removed = columns.remove(dropColumn.getColumnName());
    if (removed == null) {
      throw new IllegalStateException("Column [" + dropColumn.getColumnName() + "] does not exist for DropColumn change on table [" + dropColumn.getTableName() + "]?");
    }
  }

  public String getName() {
    return name;
  }

  /**
   * Return true if this table is a 'Draft' table.
   */
  public boolean isDraft() {
    return draft;
  }

  /**
   * Return true if this table is partitioned.
   */
  public boolean isPartitioned() {
    return partitionMeta != null;
  }

  /**
   * Return the partition meta for this table.
   */
  public PartitionMeta getPartitionMeta() {
    return partitionMeta;
  }

  public void setPkName(String pkName) {
    this.pkName = pkName;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
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

  public MTable setWithHistory(boolean withHistory) {
    this.withHistory = withHistory;
    return this;
  }

  public List<String> allHistoryColumns(boolean includeDropped) {

    List<String> columnNames = new ArrayList<>(columns.size());
    for (MColumn column : columns.values()) {
      if (column.isIncludeInHistory()) {
        columnNames.add(column.getName());
      }
    }
    if (includeDropped && !droppedColumns.isEmpty()) {
      columnNames.addAll(droppedColumns);
    }
    return columnNames;
  }

  /**
   * Return all the columns (excluding columns marked as dropped).
   */
  public Collection<MColumn> allColumns() {
    return columns.values();
  }

  /**
   * Return the column by name.
   */
  public MColumn getColumn(String name) {
    return columns.get(name);
  }

  private Map<String, MColumn> getColumns() {
    return columns;
  }

  public List<MCompoundUniqueConstraint> getUniqueConstraints() {
    return uniqueConstraints;
  }

  public List<MCompoundForeignKey> getCompoundKeys() {
    return compoundKeys;
  }

  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

  public void setSequenceInitial(int sequenceInitial) {
    this.sequenceInitial = sequenceInitial;
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
   * Return the list of columns that make the primary key.
   */
  public List<MColumn> primaryKeyColumns() {
    List<MColumn> pk = new ArrayList<>(3);
    for (MColumn column : allColumns()) {
      if (column.isPrimaryKey()) {
        pk.add(column);
      }
    }
    return pk;
  }

  /**
   * Return the primary key column if it is a simple primary key.
   */
  public String singlePrimaryKey() {
    List<MColumn> columns = primaryKeyColumns();
    if (columns.size() == 1) {
      return columns.get(0).getName();
    }
    return null;
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
   * Add a unique constraint.
   */
  public void addUniqueConstraint(String[] columns, boolean oneToOne, String constraintName) {
    uniqueConstraints.add(new MCompoundUniqueConstraint(columns, oneToOne, constraintName));
  }

  /**
   * Add a unique constraint.
   */
  public void addUniqueConstraint(List<MColumn> columns, boolean oneToOne, String constraintName) {
    String[] cols = new String[columns.size()];
    for (int i = 0; i < columns.size(); i++) {
      cols[i] = columns.get(i).getName();
    }
    addUniqueConstraint(cols, oneToOne, constraintName);
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

    MColumn existingColumn = getColumn(dbCol);
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

  /**
   * Register a pending un-applied drop column.
   * <p>
   * This means this column still needs to be included in history views/triggers etc even
   * though it is not part of the current model.
   */
  public void registerPendingDropColumn(String columnName) {
    droppedColumns.add(columnName);
  }

  private int toInt(BigInteger value) {
    return (value == null) ? 0 : value.intValue();
  }

  private BigInteger toBigInteger(int value) {
    return (value == 0) ? null : BigInteger.valueOf(value);
  }

  /**
   * Check if there are duplicate foreign keys.
   * <p>
   * This can occur when an ManyToMany relates back to itself.
   * </p>
   */
  public void checkDuplicateForeignKeys() {

    if (hasDuplicateForeignKeys()) {
      int counter = 1;
      for (MCompoundForeignKey fk : compoundKeys) {
        fk.addNameSuffix(counter++);
      }
    }
  }

  /**
   * Return true if the foreign key names are not unique.
   */
  private boolean hasDuplicateForeignKeys() {
    Set<String> fkNames = new HashSet<>();
    for (MCompoundForeignKey fk : compoundKeys) {
      if (!fkNames.add(fk.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adjust the references (FK) if it should relate to a draft table.
   */
  public void adjustReferences(ModelContainer modelContainer) {

    Collection<MColumn> cols = allColumns();
    for (MColumn col : cols) {
      String references = col.getReferences();
      if (references != null) {
        String baseTable = extractBaseTable(references);
        MTable refBaseTable = modelContainer.getTable(baseTable);
        if (refBaseTable.draftTable != null) {
          // change references to another associated 'draft' table
          String newReferences = deriveReferences(references, refBaseTable.draftTable.getName());
          col.setReferences(newReferences);
        }
      }
    }
  }

  /**
   * Return the base table name from references (table.column).
   */
  private String extractBaseTable(String references) {
    int lastDot = references.lastIndexOf('.');
    return references.substring(0, lastDot);
  }

  /**
   * Return the new references using the given draftTableName.
   * (The referenced column is the same as before).
   */
  private String deriveReferences(String references, String draftTableName) {
    int lastDot = references.lastIndexOf('.');
    return draftTableName + "." + references.substring(lastDot + 1);
  }

  /**
   * This method adds information which columns are nullable or not to the compound indices.
   */
  public void updateCompoundIndices() {
    for (MCompoundUniqueConstraint uniq : uniqueConstraints) {
      List<String> nullableColumns = new ArrayList<>();
      for (String columnName : uniq.getColumns()) {
        MColumn col = getColumn(columnName);
        if (col == null) {
          throw new IllegalStateException("Column '" + columnName + "' not found in table " + getName());
        }
        if (!col.isNotnull()) {
          nullableColumns.add(columnName);
        }
      }
      uniq.setNullableColumns(nullableColumns.toArray(new String[nullableColumns.size()]));
    }
  }

  public void removeForeignKey(String name) {
    compoundKeys.removeIf(fk -> fk.getName().equals(name));
  }

  /**
   * Clear the indexes on the foreign keys as they are covered by unique constraints.
   */
  public void clearForeignKeyIndexes() {
    for (MCompoundForeignKey compoundKey : compoundKeys) {
      compoundKey.setIndexName(null);
    }
  }

  public void setPartitionMeta(PartitionMeta partitionMeta) {
    this.partitionMeta = partitionMeta;
  }
}
