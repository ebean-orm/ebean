package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;


/**
 * Create the sql to create the intersection table and its foreign key
 * constraints.
 */
public class CreateIntersectionTable {

	private static final String NEW_LINE = "\n";

	private final DdlGenContext ctx;

	private final BeanPropertyAssocMany<?> manyProp;
	private final TableJoin intersectionTableJoin;
	private final TableJoin tableJoin;

	private StringBuilder sb = new StringBuilder();

	private StringBuilder pkeySb = new StringBuilder();

	private int foreignKeyCount;
	
	private int maxFkeyLength;
	
	public CreateIntersectionTable(DdlGenContext ctx, BeanPropertyAssocMany<?> manyProp) {
		this.ctx = ctx;
		this.manyProp = manyProp;
		this.intersectionTableJoin = manyProp.getIntersectionTableJoin();
		this.tableJoin = manyProp.getTableJoin();
		this.maxFkeyLength = ctx.getDdlSyntax().getMaxConstraintNameLength()-3;
	}

	public void build() {
		
		String createTable = buildCreateTable();
		ctx.addCreateIntersectionTable(createTable);

		foreignKeyCount = 0;
		buildFkConstraints();
	}

	private void buildFkConstraints() {

		BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
		String fk1 = buildFkConstraints(localDesc, intersectionTableJoin.columns(), true);
		ctx.addIntersectionTableFk(fk1);

		BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();
		String fk2 = buildFkConstraints(targetDesc, tableJoin.columns(), false);
		ctx.addIntersectionTableFk(fk2);

	}


	
	private String getFkNameSuffix() {

		foreignKeyCount++;

		if (foreignKeyCount > 9){
			return "_"+foreignKeyCount;
		} else {
			return "_0"+foreignKeyCount;
		}
	}
	
	private String getFkNameWithSuffix(String fkName) {
		if (fkName.length() > maxFkeyLength){
			fkName = fkName.substring(0, maxFkeyLength);
		}
		return fkName+getFkNameSuffix();
	}
	
	private String buildFkConstraints(BeanDescriptor<?> desc, TableJoinColumn[] columns, boolean direction) {

		
		StringBuilder fkBuf = new StringBuilder();

		String fkName = "fk_"+intersectionTableJoin.getTable()+"_"+desc.getBaseTable();
		
		fkName = getFkNameWithSuffix(fkName);
		
		fkBuf.append("alter table ");
		fkBuf.append(intersectionTableJoin.getTable());
		fkBuf.append(" add constraint ").append(fkName);
		
		fkBuf.append(" foreign key (");

		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				fkBuf.append(", ");
			}
			String col = direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
			fkBuf.append(col);
		}
		fkBuf.append(") references ").append(desc.getBaseTable()).append(" (");

		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				fkBuf.append(", ");
			}
			String col = !direction ? columns[i].getForeignDbColumn() : columns[i].getLocalDbColumn();
			fkBuf.append(col);
		}
		fkBuf.append(")");

		String fkeySuffix = ctx.getDdlSyntax().getForeignKeySuffix();
		if (fkeySuffix != null){
			fkBuf.append(" ");
			fkBuf.append(fkeySuffix);
		}
		fkBuf.append(";").append(NEW_LINE);

		return fkBuf.toString();
	}

	private String buildCreateTable() {

		BeanDescriptor<?> localDesc = manyProp.getBeanDescriptor();
		BeanDescriptor<?> targetDesc = manyProp.getTargetDescriptor();

		sb.append("create table ");
		sb.append(intersectionTableJoin.getTable());
		sb.append(" (").append(NEW_LINE);

		TableJoinColumn[] columns = intersectionTableJoin.columns();
		for (int i = 0; i < columns.length; i++) {

			addColumn(localDesc, columns[i].getForeignDbColumn(), columns[i].getLocalDbColumn());
		}

		TableJoinColumn[] otherColumns = tableJoin.columns();
		for (int i = 0; i < otherColumns.length; i++) {

			addColumn(targetDesc, otherColumns[i].getLocalDbColumn(), otherColumns[i].getForeignDbColumn());
		}

		sb.append("  constraint pk_").append(intersectionTableJoin.getTable());
		sb.append(" primary key (").append(pkeySb.toString().substring(2));
		sb.append("))").append(NEW_LINE).append(";").append(NEW_LINE);

		return sb.toString();
	}

	private void addColumn(BeanDescriptor<?> desc, String column, String findPropColumn) {

		pkeySb.append(", ");
		pkeySb.append(column);

		writeColumn(column);

		BeanProperty p = desc.getIdBinder().findBeanProperty(findPropColumn);
		if (p == null) {
			throw new RuntimeException("Could not find id property for " + findPropColumn);
		}

		String columnDefn = ctx.getColumnDefn(p);
		sb.append(columnDefn);
		sb.append(" not null");
		sb.append(",").append(NEW_LINE);
	}

	private void writeColumn(String columnName) {
		sb.append("  ").append(ctx.pad(columnName, 30)).append(" ");
	}
}
