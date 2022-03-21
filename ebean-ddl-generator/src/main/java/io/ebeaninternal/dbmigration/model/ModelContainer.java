package io.ebeaninternal.dbmigration.model;

import io.ebean.migration.MigrationVersion;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.DdlHelp;
import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.AddHistoryTable;
import io.ebeaninternal.dbmigration.migration.AddTableComment;
import io.ebeaninternal.dbmigration.migration.AddUniqueConstraint;
import io.ebeaninternal.dbmigration.migration.AlterColumn;
import io.ebeaninternal.dbmigration.migration.AlterForeignKey;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.ChangeSetType;
import io.ebeaninternal.dbmigration.migration.CreateIndex;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.DropHistoryTable;
import io.ebeaninternal.dbmigration.migration.DropIndex;
import io.ebeaninternal.dbmigration.migration.DropTable;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migration.RenameColumn;
import io.ebeaninternal.dbmigration.migration.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Holds all the tables, views, indexes etc that represent the model.
 * <p>
 * Migration changeSets can be applied to the model.
 * </p>
 */
public class ModelContainer {

  private final Set<String> schemas = new TreeSet<>();
  /**
   * All the tables in the model.
   */
  private final Map<String, MTable> tables = new LinkedHashMap<>();

  /**
   * All the non-unique non-foreign key indexes.
   */
  private final Map<String, MIndex> indexes = new LinkedHashMap<>();
  private final PendingDrops pendingDrops = new PendingDrops();
  private final List<MTable> partitionedTables = new ArrayList<>();

  public ModelContainer() {
  }

  /**
   * Return the schemas.
   */
  public Set<String> getSchemas() {
    return schemas;
  }

  /**
   * Return true if the model contains tables that are partitioned.
   */
  public boolean isTablePartitioning() {
    return !partitionedTables.isEmpty();
  }

  /**
   * Return the list of partitioned tables.
   */
  public List<MTable> getPartitionedTables() {
    return partitionedTables;
  }

  /**
   * Adjust the FK references on all the draft tables.
   */
  public void adjustDraftReferences() {
    for (MTable table : this.tables.values()) {
      if (table.isDraft()) {
        table.adjustReferences(this);
      }
    }
  }

  /**
   * Return the map of all the tables.
   */
  public Map<String, MTable> getTables() {
    return tables;
  }

  /**
   * Return the table by name.
   */
  public MTable getTable(String tableName) {
    return tables.get(tableName);
  }

  /**
   * Lookup the matching index during DIFF migration processing.
   */
  public MIndex getIndex(MIndex newIndex) {
    return indexes.get(newIndex.getKey());
  }

  public Collection<MIndex> allIndexes() {
    return indexes.values();
  }

  /**
   * Return true if the index does not exist and should be dropped.
   */
  public boolean dropIndex(MIndex existingIndex) {
    return !indexes.containsKey(existingIndex.getKey());
  }

  /**
   * Apply a migration with associated changeSets to the model.
   */
  public void apply(Migration migration, MigrationVersion version) {

    List<ChangeSet> changeSets = migration.getChangeSet();
    for (ChangeSet changeSet : changeSets) {
      boolean pending = changeSet.getType() == ChangeSetType.PENDING_DROPS;
      if (pending) {
        // un-applied drop columns etc
        pendingDrops.add(version, changeSet);

      } else if (isDropsFor(changeSet)) {
        pendingDrops.appliedDropsFor(changeSet);
      }
      if (!isDropsFor(changeSet)) {
        applyChangeSet(changeSet);
      }
    }
  }

  /**
   * Return true if the changeSet contains drops for a previous PENDING_DROPS changeSet.
   */
  private boolean isDropsFor(ChangeSet changeSet) {
    return changeSet.getDropsFor() != null;
  }

  /**
   * Apply a changeSet to the model.
   */
  protected void applyChangeSet(ChangeSet changeSet) {

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    for (Object change : changeSetChildren) {
      if (change instanceof CreateTable) {
        applyChange((CreateTable) change);
      } else if (change instanceof DropTable) {
        applyChange((DropTable) change);
      } else if (change instanceof AlterColumn) {
        applyChange((AlterColumn) change);
      } else if (change instanceof AddColumn) {
        applyChange((AddColumn) change);
      } else if (change instanceof DropColumn) {
        applyChange((DropColumn) change);
      } else if (change instanceof RenameColumn) {
        applyChange((RenameColumn) change);
      } else if (change instanceof CreateIndex) {
        applyChange((CreateIndex) change);
      } else if (change instanceof DropIndex) {
        applyChange((DropIndex) change);
      } else if (change instanceof AddHistoryTable) {
        applyChange((AddHistoryTable) change);
      } else if (change instanceof DropHistoryTable) {
        applyChange((DropHistoryTable) change);
      } else if (change instanceof AddUniqueConstraint) {
        applyChange((AddUniqueConstraint) change);
      } else if (change instanceof AlterForeignKey) {
        applyChange((AlterForeignKey) change);
      } else if (change instanceof AddTableComment) {
        applyChange((AddTableComment) change);
      } else if (change instanceof Sql) {
        // do nothing
      } else {
        throw new IllegalArgumentException("No rule for " + change);
      }
    }
  }

  /**
   * Set the withHistory flag on the associated base table.
   */
  private void applyChange(AddHistoryTable change) {

    MTable table = tables.get(change.getBaseTable());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getBaseTable() + "] does not exist in model?");
    }
    table.setWithHistory(true);
  }

  /**
   * Unset the withHistory flag on the associated base table.
   */
  protected void applyChange(DropHistoryTable change) {

    MTable table = tables.get(change.getBaseTable());
    if (table != null) {
      table.setWithHistory(false);
    }
  }

  private void applyChange(AddUniqueConstraint change) {
    MTable table = tables.get(change.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getTableName() + "] does not exist in model?");
    }
    if (DdlHelp.isDropConstraint(change.getColumnNames())) {
      table.getUniqueConstraints().removeIf(constraint -> constraint.getName().equals(change.getConstraintName()));
    } else {
      table.getUniqueConstraints().add(new MCompoundUniqueConstraint(change));
    }
  }

  private void applyChange(AlterForeignKey change) {
    MTable table = tables.get(change.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getName() + "] does not exist in model?");
    }
    if (DdlHelp.isDropForeignKey(change.getColumnNames())) {
      table.removeForeignKey(change.getName());
    } else {
      table.addForeignKey(change.getName(), change.getRefTableName(), change.getIndexName(), change.getColumnNames(), change.getRefColumnNames());
    }
  }

  private void applyChange(AddTableComment change) {
    MTable table = tables.get(change.getName());
    if (table == null) {
      throw new IllegalStateException("Table [" + change.getName() + "] does not exist in model?");
    }
    if (DdlHelp.isDropComment(change.getComment())) {
      table.setComment(null);
    } else {
      table.setComment(change.getComment());
    }
  }

  /**
   * Apply a CreateTable change to the model.
   */
  protected void applyChange(CreateTable createTable) {
    String tableName = createTable.getName();
    if (tables.containsKey(tableName)) {
      throw new IllegalStateException("Table [" + tableName + "] already exists in model?");
    }
    tables.put(tableName, new MTable(createTable));
  }

  /**
   * Apply a DropTable change to the model.
   */
  protected void applyChange(DropTable dropTable) {
    tables.remove(dropTable.getName());
  }

  /**
   * Apply a CreateTable change to the model.
   */
  protected void applyChange(CreateIndex createIndex) {
    String indexName = createIndex.getIndexName();
    if (indexes.containsKey(indexName)) {
      throw new IllegalStateException("Index [" + indexName + "] already exists in model?");
    }
    MIndex index = new MIndex(createIndex);
    indexes.put(index.getKey(), index);
  }

  /**
   * Apply a DropTable change to the model.
   */
  protected void applyChange(DropIndex dropIndex) {
    indexes.remove(dropIndex.getIndexName());
  }

  /**
   * Apply a AddColumn change to the model.
   */
  protected void applyChange(AddColumn addColumn) {
    MTable table = tables.get(addColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + addColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(addColumn);
  }

  /**
   * Apply a AddColumn change to the model.
   */
  protected void applyChange(AlterColumn alterColumn) {
    MTable table = tables.get(alterColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + alterColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(alterColumn);
  }

  /**
   * Apply a DropColumn change to the model.
   */
  protected void applyChange(DropColumn dropColumn) {
    MTable table = tables.get(dropColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + dropColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(dropColumn);
  }

  protected void applyChange(RenameColumn renameColumn) {
    MTable table = tables.get(renameColumn.getTableName());
    if (table == null) {
      throw new IllegalStateException("Table [" + renameColumn.getTableName() + "] does not exist in model?");
    }
    table.apply(renameColumn);
  }

  /**
   * Add a table (typically from reading EbeanServer meta data).
   */
  public MTable addTable(MTable table) {
    if (table.isPartitioned()) {
      partitionedTables.add(table);
    }
    String schema = table.getSchema();
    if (schema != null) {
      schemas.add(schema);
    }
    return tables.put(table.getName(), table);
  }

  /**
   * Add an element table taking into account if it is reused/references back
   * to multiple bean types (and so can't have foreign key).
   */
  public void addTableElementCollection(MTable table) {
    final MTable reusedElementCollection = tables.get(table.getName());
    if (reusedElementCollection != null) {
      final MIndex index = reusedElementCollection.setReusedElementCollection();
      if (index != null) {
        indexes.put(index.getKey(), index);
      }
    } else {
      if (table.isPartitioned()) {
        partitionedTables.add(table);
      }
      tables.put(table.getName(), table);
    }
  }

  /**
   * Add an index.
   */
  public void addIndex(MIndex index) {
    indexes.put(index.getKey(), index);
  }

  /**
   * Return the list of versions containing un-applied pending drops.
   */
  public List<String> getPendingDrops() {
    return pendingDrops.pendingDrops();
  }

  /**
   * Return the migration for the pending drops for a given version.
   */
  public Migration migrationForPendingDrop(String pendingVersion) {
    return pendingDrops.migrationForVersion(pendingVersion);
  }

  /**
   * Register the drop columns on history tables that have not been applied yet.
   */
  public void registerPendingHistoryDropColumns(ModelContainer newModel) {
    pendingDrops.registerPendingHistoryDropColumns(newModel);
  }

  /**
   * Register any pending drop columns on history tables.  These columns are now not in the current
   * logical model but we still need to include them in the history views and triggers until they
   * are actually dropped.
   */
  public void registerPendingHistoryDropColumns(ChangeSet changeSet) {
    for (Object change : changeSet.getChangeSetChildren()) {
      if (change instanceof DropColumn) {
        DropColumn dropColumn = (DropColumn) change;
        registerPendingDropColumn(dropColumn);
      }
    }
  }

  /**
   * Register a drop column on a history tables that has not been applied yet.
   */
  private void registerPendingDropColumn(DropColumn dropColumn) {

    MTable table = getTable(dropColumn.getTableName());
    if (table == null) {
      throw new IllegalArgumentException("Table [" + dropColumn.getTableName() + "] not found?");
    }
    table.registerPendingDropColumn(dropColumn.getColumnName());
  }
}
