package io.ebean.dbmigration.model.build;

import io.ebean.dbmigration.model.MColumn;
import io.ebean.dbmigration.model.MCompoundForeignKey;
import io.ebean.dbmigration.model.MTable;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.TableJoinInfo;
import io.ebean.plugin.TableJoinColumnInfo;
import io.ebean.plugin.Property;
import io.ebean.plugin.PropertyAssocMany;


/**
 * Add the intersection table to the model.
 */
public class ModelBuildIntersectionTable {

  private final ModelBuildContext ctx;

  private final PropertyAssocMany manyProp;
  private final TableJoinInfo intersectionTableJoin;
  private final TableJoinInfo tableJoin;

  private MTable intersectionTable;

  private int countForeignKey;

  public ModelBuildIntersectionTable(ModelBuildContext ctx, PropertyAssocMany manyProp) {
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

    if (manyProp.getTargetBeanType().isDraftable()) {
      ctx.createDraft(intersectionTable, false);
    }

  }

  private void buildFkConstraints() {

    BeanType<?> localDesc = manyProp.getBeanType();
    buildFkConstraints(localDesc, intersectionTableJoin.columns(), true);

    BeanType<?> targetDesc = manyProp.getTargetBeanType();
    buildFkConstraints(targetDesc, tableJoin.columns(), false);

    intersectionTable.checkDuplicateForeignKeys();
  }


  private void buildFkConstraints(BeanType<?> desc, TableJoinColumnInfo[] columns, boolean direction) {

    String tableName = intersectionTableJoin.getTable();
    String baseTable = ctx.normaliseTable(desc.getBaseTable());
    String fkName = ctx.foreignKeyConstraintName(tableName, baseTable, ++countForeignKey);
    String fkIndex = ctx.foreignKeyIndexName(tableName, baseTable, countForeignKey);

    MCompoundForeignKey foreignKey = new MCompoundForeignKey(fkName, desc.getBaseTable(), fkIndex);
    intersectionTable.addForeignKey(foreignKey);

    for (TableJoinColumnInfo column : columns) {
      String localCol = direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
      String refCol = !direction ? column.getForeignDbColumn() : column.getLocalDbColumn();
      foreignKey.addColumnPair(localCol, refCol);
    }
  }

  private MTable createTable() {

    BeanType<?> localDesc = manyProp.getBeanType();
    BeanType<?> targetDesc = manyProp.getTargetBeanType();

    String tableName = intersectionTableJoin.getTable();
    MTable table = new MTable(tableName);
    if (!manyProp.isExcludedFromHistory()) {
      if (localDesc.isHistorySupport()) {
        table.setWithHistory(true);
      }
    }
    table.setPkName(ctx.primaryKeyName(tableName));

    TableJoinColumnInfo[] columns = intersectionTableJoin.columns();
    for (TableJoinColumnInfo column : columns) {
      addColumn(table, localDesc, column.getForeignDbColumn(), column.getLocalDbColumn());
    }

    TableJoinColumnInfo[] otherColumns = tableJoin.columns();
    for (TableJoinColumnInfo otherColumn : otherColumns) {
      addColumn(table, targetDesc, otherColumn.getLocalDbColumn(), otherColumn.getForeignDbColumn());
    }

    return table;
  }

  private void addColumn(MTable table, BeanType<?> desc, String column, String findPropColumn) {

    Property p = desc.findIdProperty(findPropColumn);
    if (p == null) {
      throw new RuntimeException("Could not find id property for " + findPropColumn);
    }

    MColumn col = new MColumn(column, ctx.getColumnDefn(p, true), true);
    col.setPrimaryKey(true);
    table.addColumn(col);
  }

}
