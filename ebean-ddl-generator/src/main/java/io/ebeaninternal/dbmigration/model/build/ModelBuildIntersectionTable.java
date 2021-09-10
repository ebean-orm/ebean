package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.TableJoinColumn;

/**
 * Add the intersection table to the model.
 */
class ModelBuildIntersectionTable {

  private final ModelBuildContext ctx;

  private final BeanPropertyAssocMany<?> manyProp;
  private final TableJoin intersectionTableJoin;
  private final TableJoin tableJoin;

  private MTable intersectionTable;

  ModelBuildIntersectionTable(ModelBuildContext ctx, BeanPropertyAssocMany<?> manyProp) {
    this.ctx = ctx;
    this.manyProp = manyProp;
    this.intersectionTableJoin = manyProp.intersectionTableJoin();
    this.tableJoin = manyProp.tableJoin();
  }

  public MTable build() {

    intersectionTable = createTable();
    MTable existingTable = ctx.addTable(intersectionTable);
    if (existingTable != null) {
      throw new IllegalStateException("Property " + manyProp.fullName() + " has duplicate ManyToMany intersection table " + intersectionTable.getName()
        + ". Please use @JoinTable to define unique table to use");
    }

    buildFkConstraints();

    if (manyProp.targetDescriptor().isDraftable()) {
      ctx.createDraft(intersectionTable, false);
    }

    return intersectionTable;
  }

  private void buildFkConstraints() {

    if (manyProp.hasForeignKeyConstraint()) {
      ctx.fkeyBuilder(intersectionTable)
        .addForeignKey(manyProp.descriptor(), intersectionTableJoin, true)
        .addForeignKey(manyProp.targetDescriptor(), tableJoin, false);
    }
    intersectionTable.checkDuplicateForeignKeys();
  }

  private MTable createTable() {

    BeanDescriptor<?> localDesc = manyProp.descriptor();
    BeanDescriptor<?> targetDesc = manyProp.targetDescriptor();

    String tableName = intersectionTableJoin.getTable();
    MTable table = new MTable(tableName);
    if (!manyProp.isExcludedFromHistory()) {
      if (localDesc.isHistorySupport()) {
        table.setWithHistory(true);
      }
    }
    table.setPkName(ctx.primaryKeyName(tableName));

    TableJoinColumn[] columns = intersectionTableJoin.columns();
    for (TableJoinColumn column : columns) {
      addColumn(table, localDesc, column.getForeignDbColumn(), column.getLocalDbColumn());
    }

    TableJoinColumn[] otherColumns = tableJoin.columns();
    for (TableJoinColumn otherColumn : otherColumns) {
      addColumn(table, targetDesc, otherColumn.getLocalDbColumn(), otherColumn.getForeignDbColumn());
    }

    return table;
  }

  private void addColumn(MTable table, BeanDescriptor<?> desc, String column, String findPropColumn) {

    BeanProperty p = desc.idBinder().findBeanProperty(findPropColumn);
    if (p == null) {
      throw new RuntimeException("Could not find id property for " + findPropColumn);
    }

    MColumn col = new MColumn(column, ctx.getColumnDefn(p, true), true);
    col.setPrimaryKey(true);
    table.addColumn(col);
  }

}
