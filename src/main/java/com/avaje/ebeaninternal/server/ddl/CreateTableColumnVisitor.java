package com.avaje.ebeaninternal.server.ddl;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.config.TableName;
import com.avaje.ebean.config.dbplatform.DbDdlSyntax;
import com.avaje.ebean.config.dbplatform.IdType;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;
import com.avaje.ebeaninternal.server.deploy.id.ImportedId;
import com.avaje.ebeaninternal.server.lib.util.StringHelper;

/**
 * Used as part of CreateTableVisitor to generated the create table DDL script.
 */
public class CreateTableColumnVisitor extends BaseTablePropertyVisitor {

  private static final Logger logger = LoggerFactory.getLogger(CreateTableColumnVisitor.class);

  private final DdlGenContext ctx;

  private final DbDdlSyntax ddl;

  private final CreateTableVisitor parent;

  private BeanPropertyAssocOne<?> embedded;
  
  public CreateTableColumnVisitor(CreateTableVisitor parent, DdlGenContext ctx) {
    this.parent = parent;
    this.ctx = ctx;
    this.ddl = ctx.getDdlSyntax();
  }
	
  @Override
  public void visitMany(BeanPropertyAssocMany<?> p) {
    if (p.isManyToMany()) {
      if (p.getMappedBy() != null) {
        // only create on other 'owning' side

      } else {
        TableJoin intersectionTableJoin = p.getIntersectionTableJoin();

        // check if the intersection table has already been created
        String intTable = intersectionTableJoin.getTable();
        if (ctx.isProcessIntersectionTable(intTable)) {
          // build the create table and fkey constraints
          // putting the DDL into ctx for later output as we are
          // in the middle of rendering the create table DDL
          new CreateIntersectionTable(ctx, p).build();
        }
      }
    }
  }
	
  public void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p) {
    visitScalar(p);
  }

  public void visitCompound(BeanPropertyCompound p) {
    // do nothing
  }

  @Override
  public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {

    this.embedded = embedded;
    visitScalar(p);
  }

  private StringBuilder createUniqueConstraintBuffer(String table, String column) {

    String uqConstraintName = "uq_"+TableName.parse(table)+"_"+column;

    if (uqConstraintName.length() > ddl.getMaxConstraintNameLength()) {
      uqConstraintName = uqConstraintName.substring(0, ddl.getMaxConstraintNameLength());
    }

    uqConstraintName = ctx.removeQuotes(uqConstraintName);
    uqConstraintName = StringHelper.replaceString(uqConstraintName, " ", "_");

    StringBuilder constraintExpr = new StringBuilder();
    constraintExpr.append("constraint ").append(uqConstraintName).append(" unique (");

    return constraintExpr;
  }

  @Override
  public void visitOneImported(BeanPropertyAssocOne<?> p) {

    ImportedId importedId = p.getImportedId();

    TableJoinColumn[] columns = p.getTableJoin().columns();
    if (columns.length == 0) {
      String msg = "No join columns for " + p.getFullBeanName();
      throw new RuntimeException(msg);
    }

    StringBuilder constraintExpr = createUniqueConstraintBuffer(p.getBeanDescriptor().getBaseTable(), columns[0].getLocalDbColumn());

    for (int i = 0; i < columns.length; i++) {

      String dbCol = columns[i].getLocalDbColumn();

      if (i > 0) {
        constraintExpr.append(", ");
      }
      constraintExpr.append(dbCol);

      if (parent.isDbColumnWritten(dbCol)) {
        continue;
      }

      parent.writeColumnName(dbCol, p);

      BeanProperty importedProperty = importedId.findMatchImport(dbCol);
      if (importedProperty != null) {

        String columnDefn = ctx.getColumnDefn(importedProperty);
        ctx.write(columnDefn);

      } else {
        throw new RuntimeException("Imported BeanProperty not found?");
      }

      if (!p.isNullable()) {
        ctx.write(" not null");
      }
      ctx.write(",").writeNewLine();
    }
    constraintExpr.append(")");

    if (p.isOneToOne()) {
      if (ddl.isAddOneToOneUniqueContraint()) {
        parent.addUniqueConstraint(constraintExpr.toString());
      }
    }

  }

	@Override
	public void visitScalar(BeanProperty p) {

    if (p.isSecondaryTable()) {
      return;
    }

		if (parent.isDbColumnWritten(p.getDbColumn())) {
			return;
		}
		
		parent.writeColumnName(p.getDbColumn(), p);

		String columnDefn = ctx.getColumnDefn(p);
		
		boolean identity = isIdentity(p); 
		if (identity) {
		  ctx.write(ddl.getIdentityColumnDefn(columnDefn));
		} else {
		  ctx.write(columnDefn);  
		}

		if (p.isId() && ddl.isInlinePrimaryKeyConstraint()){
			ctx.write(" primary key");
			
		} else if (!p.isNullable() || p.isDDLNotNull()) {
			ctx.write(" not null");
		}

		if (identity) {
      writeIdentitySuffix();
    }
		
    if (p.isUnique() && !p.isId()) {
      parent.addUniqueConstraint(createUniqueConstraint(p));
    }

		parent.addCheckConstraint(p);
		ctx.write(",").writeNewLine();
	}

  private String createUniqueConstraint(BeanProperty p) {

    String baseTable = p.getBeanDescriptor().getBaseTable();
    if (baseTable == null) {
      // Embedded bean property that has unique constraint on it
      baseTable = embedded.getBeanDescriptor().getBaseTable();
    }
    StringBuilder expr = createUniqueConstraintBuffer(baseTable, p.getDbColumn());

    expr.append(p.getDbColumn()).append(")");
    return expr.toString();
  }

  protected void writeIdentitySuffix() {
    String identity = ddl.getIdentitySuffix();
    if (identity != null && identity.length() > 0) {
      ctx.write(" ").write(identity);
    }
  }
   
  protected boolean isIdentity(BeanProperty p) {

    if (p.isId()) {
      try {
        IdType idType = p.getBeanDescriptor().getIdType();

        if (idType.equals(IdType.IDENTITY)) {

          int jdbcType = p.getScalarType().getJdbcType();
          if (jdbcType == Types.INTEGER || jdbcType == Types.BIGINT || jdbcType == Types.SMALLINT) {

            return true;
          }
        }
      } catch (Exception e) {
        String msg = "Error determining identity on property " + p.getFullBeanName();
        logger.error(msg, e);
      }
    }
    return false;
  }

}