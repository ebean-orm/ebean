package io.ebeaninternal.dbmigration.model.build;

import io.ebean.annotation.Platform;
import io.ebeaninternal.dbmigration.ddlgeneration.platform.util.IndexSet;
import io.ebeaninternal.dbmigration.model.*;
import io.ebeaninternal.server.deploy.*;
import io.ebeaninternal.server.deploy.visitor.BaseTablePropertyVisitor;

import java.util.*;

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
    addIndexes(beanDescriptor.indexDefinitions());
  }

  /**
   * Add unique constraints defined via JPA UniqueConstraint annotations.
   */
  private void addIndexes(IndexDefinition[] indexes) {
    if (indexes != null) {
      for (IndexDefinition index : indexes) {
        String[] columns = index.getColumns();
        indexSet.add(columns);
        if (index.isUniqueConstraint()) {
          table.addUniqueConstraint(createMUniqueConstraint(index, columns));
        } else {
          // 'just' an index (not a unique constraint)
          ctx.addIndex(createMIndex(indexName(index), table.getName(), index));
        }
      }
    }
  }

  private MCompoundUniqueConstraint createMUniqueConstraint(IndexDefinition index, String[] columns) {
    return new MCompoundUniqueConstraint(columns, false, uniqueConstraintName(index), platforms(index.getPlatforms()));
  }

  private String uniqueConstraintName(IndexDefinition index) {
    String uqName = index.getName();
    if (uqName == null || uqName.trim().isEmpty()) {
      return uniqueConstraintName(index.getColumns());
    }
    return uqName;
  }

  private String indexName(IndexDefinition index) {
    String idxName = index.getName();
    if (idxName == null || idxName.trim().isEmpty()) {
      idxName = indexName(index.getColumns());
    }
    return idxName;
  }

  private MIndex createMIndex(String indexName, String tableName, IndexDefinition index) {
    return new MIndex(indexName, tableName, index.getColumns(), platforms(index.getPlatforms()), index.isUnique(), index.isConcurrent(), index.getDefinition());
  }

  private String platforms(Platform[] platforms) {
    if (platforms == null || platforms.length == 0) {
      return null;
    }
    StringJoiner joiner = new StringJoiner(",");
    for (Platform platform : platforms) {
      joiner.add(platform.name());
    }
    return joiner.toString();
  }

  @Override
  public void visitEnd() {
    // set the primary key name
    table.setPkName(primaryKeyName());

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
    if (p.createJoinTable()) {
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
          col.setUnique(uniqueConstraintName(col.getName()));
        }
      }
    } else if (p.isElementCollection()) {
      ModelBuildElementTable.build(ctx, p);
    }
  }

  @Override
  public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {
    if (p instanceof BeanPropertyAssocOne) {
      visitOneImported((BeanPropertyAssocOne<?>)p);
    } else {
      // only allow Nonnull if embedded is Nonnull
      visitScalar(p, !embedded.isNullable());
    }
    if (embedded.isId()) {
      // compound primary key
      lastColumn.setPrimaryKey(true);
    }
  }

  @Override
  public void visitOneImported(BeanPropertyAssocOne<?> p) {
    PropertyForeignKey foreignKey = p.foreignKey();
    boolean addForeignKey = foreignKey == null || !foreignKey.isNoConstraint();

    TableJoinColumn[] columns = p.tableJoin().columns();
    if (columns.length == 0) {
      throw new RuntimeException("No join columns for " + p.fullName());
    }

    List<MColumn> modelColumns = new ArrayList<>(columns.length);

    MCompoundForeignKey compoundKey = null;
    if (addForeignKey && columns.length > 1) {
      // compound foreign key
      String refTable = p.targetDescriptor().baseTable();
      String fkName = foreignKeyConstraintName(p.name());
      String fkIndex = foreignKeyIndexName(p.name());
      compoundKey = new MCompoundForeignKey(fkName, refTable, fkIndex);
      if (foreignKey != null) {
        compoundKey.setForeignKeyModes(foreignKey.getOnDelete(), foreignKey.getOnUpdate());
      }
      table.addForeignKey(compoundKey);
    }

    for (TableJoinColumn column : columns) {
      String dbCol = column.getLocalDbColumn();
      BeanProperty importedProperty = p.findMatchImport(dbCol);
      if (importedProperty == null) {
        continue;
      }
      String columnDefn = ctx.getColumnDefn(importedProperty, true);
      String refColumn = importedProperty.dbColumn();

      MColumn col = table.addColumn(dbCol, columnDefn, !p.isNullable());
      col.setDbMigrationInfos(p.dbMigrationInfos());
      col.setDefaultValue(p.dbColumnDefault());
      col.setComment(p.dbComment());
      if (addForeignKey) {
        if (columns.length > 1) {
          compoundKey.addColumnPair(dbCol, refColumn);
        } else {
          if (p.hasForeignKeyConstraint() && !importedProperty.descriptor().suppressForeignKey()) {
            // single references column (put it on the column)
            String refTable = importedProperty.descriptor().baseTable();
            if (refTable == null) {
              // odd case where an EmbeddedId only has 1 property
              refTable = p.targetDescriptor().baseTable();
            }
            col.setReferences(refTable + "." + refColumn);
            col.setForeignKeyName(foreignKeyConstraintName(col.getName()));
            if (p.hasForeignKeyIndex()) {
              col.setForeignKeyIndex(foreignKeyIndexName(col.getName()));
            }
            if (foreignKey != null) {
              col.setForeignKeyModes(foreignKey.getOnDelete(), foreignKey.getOnUpdate());
            }
          }
        }
      }
      modelColumns.add(col);
    }

    if (p.isOneToOne()) {
      // adding the unique constraint restricts the cardinality from OneToMany down to OneToOne
      // for MsSqlServer we need different DDL to handle NULL values on this constraint
      if (modelColumns.size() == 1) {
        MColumn col = modelColumns.get(0);
        indexSetAdd(col.getName());
        col.setUniqueOneToOne(uniqueConstraintName(col.getName()));

      } else {
        String[] cols = indexSetAdd(toColumnNames(modelColumns));
        String uqName = uniqueConstraintName(p.name());
        table.addUniqueConstraint(new MCompoundUniqueConstraint(cols, uqName));
      }
    }
  }

  @Override
  public void visitScalar(BeanProperty p, boolean allowNonNull) {
    if (p.isSecondaryTable()) {
      lastColumn = null;
      return;
    }
    // using non-strict mode to render the DB type such that we have a
    // "logical" type like jsonb(200) that can map to JSONB or VARCHAR(200)
    MColumn col = table.addColumnScalar(p.dbColumn(), ctx.getColumnDefn(p, false));
    //MColumn col = new MColumn(p.dbColumn(), ctx.getColumnDefn(p, false));
    col.setComment(p.dbComment());
    col.setDraftOnly(p.isDraftOnly());
    col.setHistoryExclude(p.isExcludedFromHistory());
    if (p.isId() || p.isImportedPrimaryKey()) {
      col.setPrimaryKey(true);
      if (p.descriptor().isUseIdGenerator()) {
        col.setIdentity(true);
      }
      TableJoin primaryKeyJoin = p.descriptor().primaryKeyJoin();
      if (primaryKeyJoin != null && !table.isPartitioned()) {
        final PropertyForeignKey foreignKey = primaryKeyJoin.getForeignKey();
        if (foreignKey == null || !foreignKey.isNoConstraint()) {
          TableJoinColumn[] columns = primaryKeyJoin.columns();
          col.setReferences(primaryKeyJoin.getTable() + "." + columns[0].getForeignDbColumn());
          col.setForeignKeyName(foreignKeyConstraintName(col.getName()));
          if (foreignKey != null) {
            col.setForeignKeyModes(foreignKey.getOnDelete(), foreignKey.getOnUpdate());
          }
        }
      }
    } else {
      col.setDefaultValue(p.dbColumnDefault());
      if (allowNonNull && (!p.isNullable() || p.isDDLNotNull())) {
        col.setNotnull(true);
      }
    }

    col.setDbMigrationInfos(p.dbMigrationInfos());
    if (p.isUnique() && !p.isId()) {
      col.setUnique(uniqueConstraintName(col.getName()));
      indexSetAdd(col.getName());
    }
    Set<String> checkConstraintValues = p.dbCheckConstraintValues();
    if (checkConstraintValues != null) {
      if (beanDescriptor.hasInheritance()) {
        InheritInfo inheritInfo = beanDescriptor.inheritInfo();
        inheritInfo.appendCheckConstraintValues(p.name(), checkConstraintValues);
      }
      col.setCheckConstraint(buildCheckConstraint(p.dbColumn(), checkConstraintValues));
      col.setCheckConstraintName(checkConstraintName(col.getName()));
    }
    lastColumn = col;
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
        sb.append(',');
      }
      sb.append(value);
    }
    sb.append("))");
    return sb.toString();
  }

  private void indexSetAdd(String column) {
    indexSet.add(column);
  }

  private String[] indexSetAdd(String[] cols) {
    indexSet.add(cols);
    return cols;
  }

  private String[] toColumnNames(List<MColumn> modelColumns) {
    String[] cols = new String[modelColumns.size()];
    for (int i = 0; i < modelColumns.size(); i++) {
      cols[i] = modelColumns.get(i).getName();
    }
    return cols;
  }

  /**
   * Return the primary key constraint name.
   */
  protected String primaryKeyName() {
    return ctx.primaryKeyName(table.getName());
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String foreignKeyConstraintName(String columnName) {
    return ctx.foreignKeyConstraintName(table.getName(), columnName, ++countForeignKey);
  }

  protected String foreignKeyIndexName(String column) {
    String[] cols = {column};
    return foreignKeyIndexName(cols);
  }

  /**
   * Return the foreign key constraint name given a single column foreign key.
   */
  protected String foreignKeyIndexName(String[] columns) {
    return ctx.foreignKeyIndexName(table.getName(), columns, ++countIndex);
  }

  /**
   * Return the index name given multiple columns.
   */
  protected String indexName(String[] columns) {
    return ctx.indexName(table.getName(), columns, ++countIndex);
  }

  /**
   * Return the unique constraint name.
   */
  protected String uniqueConstraintName(String columnName) {
    return ctx.uniqueConstraintName(table.getName(), columnName, ++countUnique);
  }

  /**
   * Return the unique constraint name.
   */
  protected String uniqueConstraintName(String[] columnNames) {
    return ctx.uniqueConstraintName(table.getName(), columnNames, ++countUnique);
  }

  /**
   * Return the constraint name.
   */
  protected String checkConstraintName(String columnName) {
    return ctx.checkConstraintName(table.getName(), columnName, ++countCheck);
  }

  private boolean hasValue(String val) {
    return val != null && !val.isEmpty();
  }

}
