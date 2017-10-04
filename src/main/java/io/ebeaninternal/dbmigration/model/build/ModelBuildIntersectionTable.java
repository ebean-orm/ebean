package io.ebeaninternal.dbmigration.model.build;

import io.ebeaninternal.dbmigration.model.MColumn;
import io.ebeaninternal.dbmigration.model.MCompoundForeignKey;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.TableJoin;
import io.ebeaninternal.server.deploy.TableJoinColumn;


/**
 * Add the intersection table to the model.
 */
public class ModelBuildIntersectionTable {

  private final ModelBuildContext ctx;

  private final BeanPropertyAssocMany<?> manyProp;
  private final TableJoin intersectionTableJoin;
  private final TableJoin tableJoin;

  private MTable intersectionTable;

  private int countForeignKey;

  public ModelBuildIntersectionTable(ModelBuildContext ctx, BeanPropertyAssocMany<?> manyProp) {
    this.ctx = ctx;
    this.manyProp = manyProp;
    this.intersectionTableJoin = manyProp.getIntersectionTableJoin();
    this.tableJoin = manyProp.getTableJoin();
  }

  public void build() {

    intersectionTable = createTable();
    MTable existingTable = ctx.addTable(intersectionTable);
    if (existingTable != null) {
      throw new IllegalStateException("Property " + manyProp.getFullBeanName() + " has duplicate ManyToMany intersection table " + intersectionTable.getName()
        + ". Please use @JoinTable to define unique table to use");
    }

    buildFkConstraints();

    if (manyProp.getTargetDescriptor().isDraftable()) {
      ctx.createDraft(intersectionTable, false);
    }

  }

  private void buildFkConstraints() {

    BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
    buildFkConstraints(localDesc, intersectionTableJoin.columns(), true);

    BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();
    buildFkConstraints(targetDesc, tableJoin.columns(), false);

    intersectionTable.checkDuplicateForeignKeys();
  }


  private void buildFkConstraints(BeanDescriptor<?> desc, TableJoinColumn[] columns, boolean direction) {

    String tableName = intersectionTableJoin.getTable();
    String baseTable = ctx.normaliseTable(desc.getBaseTable());
    String fkName = ctx.foreignKeyConstraintName(tableName, baseTable, ++countForeignKey);
    String fkIndex = ctx.foreignKeyIndexName(tableName, baseTable, countForeignKey);

    MCompoundForeignKey foreignKey = new MCompoundForeignKey(fkName, desc.getBaseTable(), fkIndex);
    intersectionTable.addForeignKey(foreignKey);

    for (TableJoinColumn column : columns) {
      String localCol = direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
      String refCol = !direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
      foreignKey.addColumnPair(localCol, refCol);
    }
  }

  private MTable createTable() {

    BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
    BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();

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

    BeanProperty p = desc.getIdBinder().findBeanProperty(findPropColumn);
    if (p == null) {
      throw new RuntimeException("Could not find id property for " + findPropColumn);
    }

    MColumn col = new MColumn(column, ctx.getColumnDefn(p, true), true);
    col.setPrimaryKey(true);
    table.addColumn(col);
  }

}
