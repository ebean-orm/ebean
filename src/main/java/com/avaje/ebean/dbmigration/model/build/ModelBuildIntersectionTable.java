package com.avaje.ebean.dbmigration.model.build;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;
import com.avaje.ebean.dbmigration.model.MColumn;
import com.avaje.ebean.dbmigration.model.MTable;


/**
 * Add the intersection table to the model.
 */
public class ModelBuildIntersectionTable {

	private final ModelBuildContext ctx;

	private final BeanPropertyAssocMany<?> manyProp;
	private final TableJoin intersectionTableJoin;
	private final TableJoin tableJoin;

	public ModelBuildIntersectionTable(ModelBuildContext ctx, BeanPropertyAssocMany<?> manyProp) {
		this.ctx = ctx;
		this.manyProp = manyProp;
		this.intersectionTableJoin = manyProp.getIntersectionTableJoin();
		this.tableJoin = manyProp.getTableJoin();
	}

	public void build() {

    MTable table = createTable();
    ctx.addTable(table);

    //buildFkConstraints();
	}

//	private void buildFkConstraints(MTable table) {
//
//		BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
//		String fk1 = buildFkConstraints(localDesc, intersectionTableJoin.columns(), true);
//		ctx.addIntersectionTableFk(fk1);
//
//		BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();
//		String fk2 = buildFkConstraints(targetDesc, tableJoin.columns(), false);
//		ctx.addIntersectionTableFk(fk2);
//	}

	
//	private String buildFkConstraints(BeanDescriptor<?> desc, TableJoinColumn[] columns, boolean direction) {
//
//
//		String fkName = "fk_"+intersectionTableJoin.getTable()+"_"+desc.getBaseTable();
//
//		fkName = getFkNameWithSuffix(fkName);
//
//		fkBuf.append("alter table ");
//		fkBuf.append(intersectionTableJoin.getTable());
//		fkBuf.append(" add constraint ").append(fkName);
//
//		fkBuf.append(" foreign key (");
//
//		for (int i = 0; i < columns.length; i++) {
//			if (i > 0) {
//				fkBuf.append(", ");
//			}
//			String col = direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
//			fkBuf.append(col);
//		}
//		fkBuf.append(") references ").append(desc.getBaseTable()).append(" (");
//
//		for (int i = 0; i < columns.length; i++) {
//			if (i > 0) {
//				fkBuf.append(", ");
//			}
//			String col = !direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
//			fkBuf.append(col);
//		}
//		fkBuf.append(")");
//
//		String fkeySuffix = ctx.getDdlSyntax().getForeignKeySuffix();
//		if (fkeySuffix != null){
//			fkBuf.append(" ");
//			fkBuf.append(fkeySuffix);
//		}
//		fkBuf.append(";").append(NEW_LINE);
//
//		return fkBuf.toString();
//	}

	private MTable createTable() {

		BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
		BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();

    MTable table = new MTable(intersectionTableJoin.getTable());

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
