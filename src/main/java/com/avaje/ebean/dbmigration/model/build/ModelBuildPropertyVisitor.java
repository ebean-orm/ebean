package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.dbmigration.ddlgeneration.platform.util.IndexSet;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MCompoundForeignKey;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebean.dbmigration.model.visitor.BaseTablePropertyVisitor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.CompoundUniqueConstraint;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;

import java.util.ArrayList;
import java.util.List;

/**
 * Used as part of ModelBuildBeanVisitor and generally adds the MColumn to the associated
 * MTable model objects.
 */
public class ModelBuildPropertyVisitor extends BaseTablePropertyVisitor {

  protected final ModelBuildContext ctx;

  private final MTable table;

  private final IndexSet indexSet = new IndexSet();

  private MColumn lastColumn;

  private int countForeignKey;
  private int countIndex;
  private int countUnique;
  private int countCheck;


  public ModelBuildPropertyVisitor(ModelBuildContext ctx, MTable table, CompoundUniqueConstraint[] constraints) {
    this.ctx = ctx;
    this.table = table;

    addCompoundUniqueConstraint(constraints);
  }

  /**
   * Add unique constraints defined via JPA UniqueConstraint annotations.
   */
  private void addCompoundUniqueConstraint(CompoundUniqueConstraint[] constraints) {

    if (constraints != null) {
      for (int i = 0; i < constraints.length; i++) {
        CompoundUniqueConstraint constraint = constraints[i];
        String[] columns = constraint.getColumns();
        indexSet.add(columns);

        if (constraint.isUnique()) {
          String uqName = constraint.getName();
          if (uqName == null || uqName.trim().isEmpty()) {
            uqName = determineUniqueConstraintName(columns);
          }
          table.addCompoundUniqueConstraint(columns, false, uqName);

        } else {
          // 'just' an index (not a unique constraint)
          String idxName = constraint.getName();
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
    for (MColumn column : table.getColumns().values()) {
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
  }


  @Override
  public void visitMany(BeanPropertyAssocMany<?> p) {
    if (p.isManyToMany()) {
      if (p.getMappedBy() == null) {
        // only create on other 'owning' side

        //TableJoin intersectionTableJoin = p.getIntersectionTableJoin();
        // check if the intersection table has already been created

        // build the create table and fkey constraints
        // putting the DDL into ctx for later output as we are
        // in the middle of rendering the create table DDL
        new ModelBuildIntersectionTable(ctx, p).build();
      }
    }
  }

  @Override
  public void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p) {
    visitScalar(p);
  }

  @Override
  public void visitCompound(BeanPropertyCompound p) {
    // do nothing
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
      String msg = "No join columns for " + p.getFullBeanName();
      throw new RuntimeException(msg);
    }

    ImportedId importedId = p.getImportedId();

    List<MColumn> modelColumns = new ArrayList<MColumn>(columns.length);

    MCompoundForeignKey compoundKey = null;
    if (columns.length > 1) {
      // compound foreign key
      String refTable = p.getTargetDescriptor().getBaseTable();
      String fkName = determineForeignKeyConstraintName(p.getName());
      String fkIndex = determineForeignKeyIndexName(p.getName());
      compoundKey = new MCompoundForeignKey(fkName, refTable, fkIndex);
      table.addForeignKey(compoundKey);
    }

    for (int i = 0; i < columns.length; i++) {

      String dbCol = columns[i].getLocalDbColumn();
      BeanProperty importedProperty = importedId.findMatchImport(dbCol);
      if (importedProperty == null) {
        throw new RuntimeException("Imported BeanProperty not found?");
      }
      String columnDefn = ctx.getColumnDefn(importedProperty);
      String refColumn = importedProperty.getDbColumn();

      MColumn col = table.addColumn(dbCol, columnDefn, !p.isNullable());

      if (columns.length == 1) {
        // single references column (put it on the column)
        String refTable = importedProperty.getBeanDescriptor().getBaseTable();
        if (refTable == null) {
          // odd case where an EmbeddedId only has 1 property
          refTable = p.getTargetDescriptor().getBaseTable();
        }
        col.setReferences(refTable + "." + refColumn);
        col.setForeignKeyName(determineForeignKeyConstraintName(col.getName()));
        col.setForeignKeyIndex(determineForeignKeyIndexName(col.getName()));
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
        table.addCompoundUniqueConstraint(modelColumns, true, uqName);
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

    MColumn col = new MColumn(p.getDbColumn(), ctx.getColumnDefn(p));

    if (p.isId()) {
      col.setPrimaryKey(true);
      if (p.getBeanDescriptor().isUseIdGenerator()) {
        col.setIdentity(true);
      }
    } else if (!p.isNullable() || p.isDDLNotNull()) {
      col.setNotnull(true);
    }

    if (p.isUnique() && !p.isId()) {
      col.setUnique(determineUniqueConstraintName(col.getName()));
      indexSetAdd(col.getName());
    }
    String checkConstraint = p.getDbConstraintExpression();
    if (checkConstraint != null) {
      col.setCheckConstraint(checkConstraint);
      col.setCheckConstraintName(determineCheckConstraintName(col.getName()));
    }

    String indexName = p.getIndexName();
    if (indexName != null) {
      // single column non-unique index
      if (indexName.trim().isEmpty()) {
        indexName = determineIndexName(col.getName());
      }
      ctx.addIndex(indexName, table.getName(), p.getDbColumn());
    }

    lastColumn = col;
    table.addColumn(col);
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