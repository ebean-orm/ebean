package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.IndexSet;
import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MCompoundForeignKey;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.dbmigration.model.visitor.BaseTablePropertyVisitor;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.deploy.IndexDefinition;
import io.ebeaninternal.server.deploy.InheritInfo;
import io.ebeaninternal.server.deploy.PropertyForeignKey;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.TableJoinColumn;
import io.ebeaninternal.server.deploy.id.ImportedId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Used as part of ModelBuildBeanVisitor and generally adds the MColumn to the associated
 * MTable model objects.
 */
public class ModelBuildPropertyVisitor extends BaseTablePropertyVisitor {

  protected final ModelBuildContext ctx;

  private final MTable table;

  private final BeanDescriptor<?> beanDescriptor;

  private final IndexSet indexSet = new IndexSet();

  private MColumn lastColumn;

  private int countForeignKey;
  private int countIndex;
  private int countUnique;
  private int countCheck;


  public ModelBuildPropertyVisitor(ModelBuildContext ctx, MTable table, BeanDescriptor<?> beanDescriptor) {
    this.ctx = ctx;
    this.table = table;
    this.beanDescriptor = beanDescriptor;
    addIndexes(beanDescriptor.getIndexDefinitions());
  }

  /**
   * Add unique constraints defined via JPA UniqueConstraint annotations.
   */
  private void addIndexes(IndexDefinition[] indexes) {

    if (indexes != null) {
      for (IndexDefinition index : indexes) {
        String[] columns = index.getColumns();
        indexSet.add(columns);

        if (index.isUnique()) {
          String uqName = index.getName();
          if (uqName == null || uqName.trim().isEmpty()) {
            uqName = determineUniqueConstraintName(columns);
          }
          table.addUniqueConstraint(columns, false, uqName);

        } else {
          // 'just' an index (not a unique constraint)
          String idxName = index.getName();
          if (idxName == null || idxName.trim().isEmpty()) {
            idxName = determineIndexName(columns);
          }
          ctx.addIndex(idxName, table.getName(), columns);
        }
      }
    }
  }

  @Override
  public void visitEnd() {

    // set the primary key name
    table.setPkName(determinePrimaryKeyName());

    // check if indexes on foreign keys should be suppressed
    for (MColumn column : table.allColumns()) {
      if (hasValue(column.getForeignKeyIndex())) {
        if (indexSet.contains(column.getName())) {
          // suppress index on foreign key as there is already
          // effectively an index (probably via unique constraint)
          column.setForeignKeyIndex(null);
        }
      }
    }

    for (MCompoundForeignKey compoundKey : table.getCompoundKeys()) {
      if (indexSet.contains(compoundKey.getColumns())) {
        // suppress index on foreign key as there is already
        // effectively an index (probably via unique constraint)
        compoundKey.setIndexName(null);
      }
    }

    addDraftTable();

    table.updateCompoundIndices();
  }

  /**
   * Create a 'draft' table that is mostly the same as the base table.
   * It has @DraftOnly columns and adjusted primary and foreign keys.
   */
  private void addDraftTable() {
    if (beanDescriptor.isDraftable() || beanDescriptor.isDraftableElement()) {
      // create a 'Draft' table which looks very similar (change PK, FK etc)
      ctx.createDraft(table, !beanDescriptor.isDraftableElement());
    }
  }


  @Override
  public void visitMany(BeanPropertyAssocMany<?> p) {
    if (p.hasJoinTable() && p.getMappedBy() == null) {
      // only create on other 'owning' side

      // build the create table and fkey constraints
      // putting the DDL into ctx for later output as we are
      // in the middle of rendering the create table DDL
      MTable intersectionTable = new ModelBuildIntersectionTable(ctx, p).build();
      if (p.isO2mJoinTable()) {
        intersectionTable.clearForeignKeyIndexes();
        Collection<MColumn> cols = intersectionTable.allColumns();
        if (cols.size() == 2) {
          // always the second column that we put the unique constraint on
          MColumn col = new ArrayList<>(cols).get(1);
          col.setUnique(determineUniqueConstraintName(col.getName()));
        }
      }
    } else if (p.isElementCollection()) {
      ModelBuildElementTable.build(ctx, p);
    }
  }

  @Override
  public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {

    visitScalar(p);
    if (embedded.isId()) {
      // compound primary key
      lastColumn.setPrimaryKey(true);
    }
  }

  @Override
  public void visitOneImported(BeanPropertyAssocOne<?> p) {

    TableJoinColumn[] columns = p.getTableJoin().columns();
    if (columns.length == 0) {
      throw new RuntimeException("No join columns for " + p.getFullBeanName());
    }

    ImportedId importedId = p.getImportedId();

    List<MColumn> modelColumns = new ArrayList<>(columns.length);

    PropertyForeignKey foreignKey = p.getForeignKey();

    MCompoundForeignKey compoundKey = null;
    if (columns.length > 1) {
      // compound foreign key
      String refTable = p.getTargetDescriptor().getBaseTable();
      String fkName = determineForeignKeyConstraintName(p.getName());
      String fkIndex = determineForeignKeyIndexName(p.getName());
      compoundKey = new MCompoundForeignKey(fkName, refTable, fkIndex);
      table.addForeignKey(compoundKey);
    }

    for (TableJoinColumn column : columns) {

      String dbCol = column.getLocalDbColumn();
      BeanProperty importedProperty = importedId.findMatchImport(dbCol);
      if (importedProperty == null) {
        throw new RuntimeException("Imported BeanProperty not found?");
      }
      String columnDefn = ctx.getColumnDefn(importedProperty, true);
      String refColumn = importedProperty.getDbColumn();

      MColumn col = table.addColumn(dbCol, columnDefn, !p.isNullable());
      col.setDbMigrationInfos(p.getDbMigrationInfos());
      col.setDefaultValue(p.getDbColumnDefault());
      if (columns.length == 1) {
        if (p.hasForeignKey() && !importedProperty.getBeanDescriptor().suppressForeignKey()) {
          // single references column (put it on the column)
          String refTable = importedProperty.getBeanDescriptor().getBaseTable();
          if (refTable == null) {
            // odd case where an EmbeddedId only has 1 property
            refTable = p.getTargetDescriptor().getBaseTable();
          }
          col.setReferences(refTable + "." + refColumn);
          col.setForeignKeyName(determineForeignKeyConstraintName(col.getName()));
          if (p.hasForeignKeyIndex()) {
            col.setForeignKeyIndex(determineForeignKeyIndexName(col.getName()));
          }
          if (foreignKey != null) {
            col.setForeignKeyModes(foreignKey.getOnDelete(), foreignKey.getOnUpdate());
          }
        }
      } else {
        compoundKey.addColumnPair(dbCol, refColumn);
      }
      modelColumns.add(col);
    }

    if (p.isOneToOne()) {
      // adding the unique constraint restricts the cardinality from OneToMany down to OneToOne
      // for MsSqlServer we need different DDL to handle NULL values on this constraint
      if (modelColumns.size() == 1) {
        MColumn col = modelColumns.get(0);
        col.setUniqueOneToOne(determineUniqueConstraintName(col.getName()));
        indexSetAdd(col.getName());

      } else {
        String uqName = determineUniqueConstraintName(p.getName());
        table.addUniqueConstraint(modelColumns, true, uqName);
        indexSetAdd(modelColumns);
      }
    }
  }

  @Override
  public void visitScalar(BeanProperty p) {

    if (p.isSecondaryTable()) {
      lastColumn = null;
      return;
    }

    // using non-strict mode to render the DB type such that we have a
    // "logical" type like jsonb(200) that can map to JSONB or VARCHAR(200)
    MColumn col = new MColumn(p.getDbColumn(), ctx.getColumnDefn(p, false));
    col.setComment(p.getDbComment());
    col.setDraftOnly(p.isDraftOnly());
    col.setHistoryExclude(p.isExcludedFromHistory());

    if (p.isId()) {
      col.setPrimaryKey(true);
      if (p.getBeanDescriptor().isUseIdGenerator()) {
        col.setIdentity(true);
      }
      TableJoin primaryKeyJoin = p.getBeanDescriptor().getPrimaryKeyJoin();
      if (primaryKeyJoin != null && !table.isPartitioned()) {
        TableJoinColumn[] columns = primaryKeyJoin.columns();
        col.setReferences(primaryKeyJoin.getTable() + "." + columns[0].getForeignDbColumn());
        col.setForeignKeyName(determineForeignKeyConstraintName(col.getName()));
      }
    } else {
      col.setDefaultValue(p.getDbColumnDefault());
      if (!p.isNullable() || p.isDDLNotNull()) {
        col.setNotnull(true);
      }
    }

    col.setDbMigrationInfos(p.getDbMigrationInfos());

    if (p.isUnique() && !p.isId()) {
      col.setUnique(determineUniqueConstraintName(col.getName()));
      indexSetAdd(col.getName());
    }
    Set<String> checkConstraintValues = p.getDbCheckConstraintValues();
    if (checkConstraintValues != null) {
      if (beanDescriptor.hasInheritance()) {
        InheritInfo inheritInfo = beanDescriptor.getInheritInfo();
        inheritInfo.appendCheckConstraintValues(p.getName(), checkConstraintValues);
      }
      col.setCheckConstraint(buildCheckConstraint(p.getDbColumn(), checkConstraintValues));
      col.setCheckConstraintName(determineCheckConstraintName(col.getName()));
    }

    lastColumn = col;
    table.addColumn(col);
  }

  /**
   * Build the check constraint clause given the db column and values.
   */
  private String buildCheckConstraint(String dbColumn, Set<String> checkConstraintValues) {
    StringBuilder sb = new StringBuilder();
    sb.append("check ( ").append(dbColumn).append(" in (");
    int count = 0;
    for (String value : checkConstraintValues) {
      if (count++ > 0) {
        sb.append(",");
      }
      sb.append(value);
    }
    sb.append("))");
    return sb.toString();
  }

  private void indexSetAdd(String column) {
    indexSet.add(column);
  }

  private void indexSetAdd(List<MColumn> modelColumns) {
    String[] cols = new String[modelColumns.size()];
    for (int i = 0; i < modelColumns.size(); i++) {
      cols[i] = modelColumns.get(i).getName();
    }
    indexSet.add(cols);
  }

  /**
   * Return the primary key constraint name.
   */
  protected String determinePrimaryKeyName() {

    return ctx.primaryKeyName(table.getName());
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyConstraintName(String columnName) {

    return ctx.foreignKeyConstraintName(table.getName(), columnName, ++countForeignKey);
  }

  protected String determineForeignKeyIndexName(String column) {

    String[] cols = {column};
    return determineForeignKeyIndexName(cols);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String determineForeignKeyIndexName(String[] columns) {

    return ctx.foreignKeyIndexName(table.getName(), columns, ++countIndex);
  }

  /**
   * Return the index name given a single column foreign key.
   */
  protected String determineIndexName(String column) {

    return ctx.indexName(table.getName(), column, ++countIndex);
  }

  /**
   * Return the index name given multiple columns.
   */
  protected String determineIndexName(String[] columns) {

    return ctx.indexName(table.getName(), columns, ++countIndex);
  }

  /**
   * Return the unique constraint name.
   */
  protected String determineUniqueConstraintName(String columnName) {

    return ctx.uniqueConstraintName(table.getName(), columnName, ++countUnique);
  }

  /**
   * Return the unique constraint name.
   */
  protected String determineUniqueConstraintName(String[] columnNames) {

    return ctx.uniqueConstraintName(table.getName(), columnNames, ++countUnique);
  }

  /**
   * Return the constraint name.
   */
  protected String determineCheckConstraintName(String columnName) {

    return ctx.checkConstraintName(table.getName(), columnName, ++countCheck);
  }


  private boolean hasValue(String val) {
    return val != null && !val.isEmpty();
  }

}
