package com.avaje.ebeaninternal.server.ddl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.TableName;
import com.avaje.ebean.config.dbplatform.DbDdlSyntax;
import com.avaje.ebean.config.dbplatform.DbType;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.CompoundUniqueContraint;
import com.avaje.ebeaninternal.server.deploy.InheritInfo;
import com.avaje.ebeaninternal.server.deploy.parse.SqlReservedWords;

/**
 * Used to generated the create table DDL script.
 */
public class CreateTableVisitor extends AbstractBeanVisitor {
	
	private static final Logger logger = LoggerFactory.getLogger(CreateTableVisitor.class);
	
	private final DdlGenContext ctx;
	
	private final PropertyVisitor pv;
	
	private final DbDdlSyntax ddl;
	
	private final int columnNameWidth;

	// avoid writing columns twice, e.g. when used in associations with insertable=false and updateable=false
	private final Set<String> wroteColumns = new HashSet<String>();

  private ArrayList<String> checkConstraints = new ArrayList<String>();

  private ArrayList<String> uniqueConstraints = new ArrayList<String>();

  private String table;
    
	public Set<String> getWroteColumns() {
		return wroteColumns;
	}
	

	public CreateTableVisitor(DdlGenContext ctx) {
		this.ctx = ctx;
		this.ddl = ctx.getDdlSyntax();
		this.columnNameWidth = ddl.getColumnNameWidth();
		this.pv = new CreateTableColumnVisitor(this, ctx);
	}
	
  public boolean isDbColumnWritten(String dbColumn) {
    // Columns are not case sensitive - user lower case
    // as e.g. @JoinColumn(s) may use a different case
    return wroteColumns.contains(dbColumn.toLowerCase());
  }
	
  public void addDbColumnWritten(String dbColumn) {
    // Column names are case insensitive
    wroteColumns.add(dbColumn.toLowerCase());
  }

  /**
   * Write the table name including a check for SQL reserved words.
   */
  protected void writeTableName(BeanDescriptor<?> descriptor) {
    
    // parse to remove any catalog or schema prefix on the table
    table = TableName.parse(descriptor.getBaseTable());
    ctx.write(descriptor.getBaseTable());
  }


  /**
   * Write the column name including a check for the SQL reserved words.
   */
  protected void writeColumnName(String columnName, BeanProperty p) {

    addDbColumnWritten(columnName);

    if (SqlReservedWords.isKeyword(columnName)) {
      String propName = p == null ? "(Unknown)" : p.getFullBeanName();
      logger.warn("Column name [" + columnName + "] is a suspected SQL reserved word for property " + propName);
    }

    ctx.write("  ").write(columnName, columnNameWidth).write(" ");
  }
	
  /**
   * Build a check constraint for the property if required.
   * <p>
   * Typically check constraint based on Enum mapping values.
   * </p>
   */
  protected void addCheckConstraint(BeanProperty p, String prefix, String constraintExpression) {

    if (p != null && constraintExpression != null) {

      // build constraint clause
      String s = "constraint " + getConstraintName(prefix, p) + " " + constraintExpression;

      // add to list as we render all check constraints just prior to primary key
      checkConstraints.add(s);
    }
  }

  protected String getConstraintName(String prefix, BeanProperty p) {
    return prefix + table + "_" + p.getDbColumn();
  }

  protected void addUniqueConstraint(String constraintExpression) {
    uniqueConstraints.add(constraintExpression);
  }

  protected void addCheckConstraint(String constraintExpression) {
    checkConstraints.add(constraintExpression);
  }

  protected void addCheckConstraint(BeanProperty p) {
    addCheckConstraint(p, "ck_", p.getDbConstraintExpression());
  }

  public boolean visitBean(BeanDescriptor<?> descriptor) {

    wroteColumns.clear();

    if (!descriptor.isInheritanceRoot()) {
      return false;
    }

    ctx.write("create table ");
    writeTableName(descriptor);
    ctx.write(" (").writeNewLine();

    InheritInfo inheritInfo = descriptor.getInheritInfo();
    if (inheritInfo != null && inheritInfo.isRoot()) {
      String discColumn = inheritInfo.getDiscriminatorColumn();
      int discType = inheritInfo.getDiscriminatorType();
      int discLength = inheritInfo.getDiscriminatorLength();
      DbType dbType = ctx.getDbTypeMap().get(discType);
      String discDbType = dbType.renderType(discLength, 0);

      writeColumnName(discColumn, null);
      ctx.write(discDbType);
      ctx.write(" not null,");
      ctx.writeNewLine();
    }

    return true;
  }

  public void visitBeanEnd(BeanDescriptor<?> descriptor) {

    visitInheritanceProperties(descriptor, pv);

    if (checkConstraints.size() > 0) {
      for (String checkConstraint : checkConstraints) {
        ctx.write("  ").write(checkConstraint).write(",").writeNewLine();
      }
      checkConstraints = new ArrayList<String>();
    }

    if (uniqueConstraints.size() > 0) {
      for (String constraint : uniqueConstraints) {
        ctx.write("  ").write(constraint).write(",").writeNewLine();
      }
      uniqueConstraints = new ArrayList<String>();
    }

    CompoundUniqueContraint[] compoundUniqueConstraints = descriptor.getCompoundUniqueConstraints();
    if (compoundUniqueConstraints != null) {
      String table = descriptor.getBaseTable();
      for (int i = 0; i < compoundUniqueConstraints.length; i++) {
        String constraint = createUniqueConstraint(table, i, compoundUniqueConstraints[i]);
        ctx.write("  ").write(constraint).write(",").writeNewLine();
      }
    }

    BeanProperty idProp = descriptor.getIdProperty();

    if (idProp == null) {
      // No comma + new line
      ctx.removeLast().removeLast();
    } else if (ddl.isInlinePrimaryKeyConstraint()) {
      // The Primary Key constraint was inlined with the column
      // ... No comma + new line
      ctx.removeLast().removeLast();

    } else {
      // Add the primay key constraint
      String pkName = ddl.getPrimaryKeyName(table);
      ctx.write("  constraint ").write(pkName).write(" primary key (");

      VisitorUtil.visit(idProp, new AbstractPropertyVisitor() {

        @Override
        public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {
          ctx.write(p.getDbColumn()).write(", ");
        }

        @Override
        public void visitScalar(BeanProperty p) {
          ctx.write(p.getDbColumn()).write(", ");
        }

        @Override
        public void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p) {
          ctx.write(p.getDbColumn()).write(", ");
        }

      });
      // remove the last comma, end of PK
      ctx.removeLast().write(")");
    }

    // end of table
    ctx.write(")").writeNewLine();
    ctx.write(";").writeNewLine().writeNewLine();
    ctx.flush();
  }

  private String createUniqueConstraint(String table, int idx, CompoundUniqueContraint uc) {

    StringBuilder sb = new StringBuilder(50);
    sb.append("constraint ")
      .append("uq_").append(TableName.parse(table)).append("_").append(idx + 1)
      .append(" unique (");

    String[] columns = uc.getColumns();

    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(columns[i]);
    }
    sb.append(")");

    return sb.toString();
  }

  public void visitBeanDescriptorEnd() {
    ctx.write(");").writeNewLine().writeNewLine();
  }

  public PropertyVisitor visitProperty(BeanProperty p) {
    return pv;
  }

  public void visitBegin() {

  }

  public void visitEnd() {
    ctx.addIntersectionCreateTables();
    ctx.flush();
  }

}
