package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MCompoundForeignKey;
import com.avaje.ebean.dbmigration.model.MTable;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;


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

		for (int i = 0; i < columns.length; i++) {
			String localCol = direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
      String refCol = !direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
      foreignKey.addColumnPair(localCol, refCol);
		}
  }

	private MTable createTable() {

		BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
		BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();

    String tableName = intersectionTableJoin.getTable();
    MTable table = new MTable(tableName);
    table.setPkName(ctx.primaryKeyName(tableName));

		TableJoinColumn[] columns = intersectionTableJoin.columns();
		for (int i = 0; i < columns.length; i++) {
			addColumn(table, localDesc, columns[i].getForeignDbColumn(), columns[i].getLocalDbColumn());
		}

		TableJoinColumn[] otherColumns = tableJoin.columns();
		for (int i = 0; i < otherColumns.length; i++) {
			addColumn(table, targetDesc, otherColumns[i].getLocalDbColumn(), otherColumns[i].getForeignDbColumn());
		}

    return table;
	}

	private void addColumn(MTable table, BeanDescriptor<?> desc, String column, String findPropColumn) {

		BeanProperty p = desc.getIdBinder().findBeanProperty(findPropColumn);
		if (p == null) {
			throw new RuntimeException("Could not find id property for " + findPropColumn);
		}

    MColumn col = new MColumn(column, ctx.getColumnDefn(p), true);
    col.setPrimaryKey(true);
    table.addColumn(col);
	}

}
