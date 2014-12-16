package com.avaje.ebeaninternal.server.ddl;

import com.avaje.ebean.config.dbplatform.DbDdlSyntax;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import com.avaje.ebeaninternal.server.deploy.BeanPropertyCompound;
import com.avaje.ebeaninternal.server.deploy.TableJoin;
import com.avaje.ebeaninternal.server.deploy.TableJoinColumn;

/**
 * Used to generate the foreign key DDL and related indexes.
 */
public class AddForeignKeysVisitor extends AbstractBeanVisitor {

	final DdlGenContext ctx;

	final FkeyPropertyVisitor pv;

	public AddForeignKeysVisitor(boolean addMode, DdlGenContext ctx) {
		this.ctx = ctx;
		this.pv = new FkeyPropertyVisitor(addMode, this, ctx);
	}

  public boolean visitBean(BeanDescriptor<?> descriptor) {
    if (!descriptor.isInheritanceRoot()) {
      // ignore/skip if not a top level BeanDescriptor
      return false;
    }
    return true;
  }

  public void visitBeanEnd(BeanDescriptor<?> descriptor) {

    visitInheritanceProperties(descriptor, pv);
  }

	public void visitBegin() {
	}

	public void visitEnd() {
		ctx.addIntersectionFkeys();
	}

	public PropertyVisitor visitProperty(BeanProperty p) {
		return pv;
	}


	public static class FkeyPropertyVisitor extends BaseTablePropertyVisitor {

    /**
     * Set to false when generating drop foreign key statements.
     */
    final boolean addMode;

		final DdlGenContext ctx;

		final AddForeignKeysVisitor parent;

		public FkeyPropertyVisitor(boolean addMode, AddForeignKeysVisitor parent, DdlGenContext ctx) {
      this.addMode = addMode;
			this.parent = parent;
			this.ctx = ctx;
		}

		@Override
		public void visitEmbeddedScalar(BeanProperty p, BeanPropertyAssocOne<?> embedded) {
			// not interested
		}

		@Override
		public void visitOneImported(BeanPropertyAssocOne<?> p) {

			// alter table {basetable} add foreign key (...) references {} (...) on delete restrict on update restrict;
			// Alter table o_address add Foreign Key (country_code) references o_country (code) on delete  restrict on update  restrict;

			String baseTable = p.getBeanDescriptor().getBaseTable();
			TableJoin tableJoin = p.getTableJoin();
			TableJoinColumn[] columns = tableJoin.columns();

			String tableName = p.getBeanDescriptor().getBaseTable();
      DbDdlSyntax ddlSyntax = ctx.getDdlSyntax();
      String fkName = ddlSyntax.getForeignKeyName(tableName, p.getName(), ctx.incrementFkCount());

      if (!addMode) {
        // look to generate drop foreign key statement
        String dropKeyConstraintPrefix = ddlSyntax.dropKeyConstraintPrefix(tableName, fkName);
        if (dropKeyConstraintPrefix != null && !dropKeyConstraintPrefix.isEmpty()) {
          ctx.write(dropKeyConstraintPrefix).write(" ");
        }
      }

      ctx.write("alter table ").write(baseTable).write(addOrDrop());
			if (fkName != null) {
				ctx.write("constraint ").write(fkName).write(" ");
			}
      if (addMode) {
        ctx.write("foreign key (");
        for (int i = 0; i < columns.length; i++) {
          if (i > 0) {
            ctx.write(",");
          }
          ctx.write(columns[i].getLocalDbColumn());
        }
        ctx.write(")");

        ctx.write(" references ");
        ctx.write(tableJoin.getTable());
        ctx.write(" (");
        for (int i = 0; i < columns.length; i++) {
          if (i > 0) {
            ctx.write(",");
          }
          ctx.write(columns[i].getForeignDbColumn());
        }
        ctx.write(")");

        String fkeySuffix = ctx.getDdlSyntax().getForeignKeySuffix();
        if (fkeySuffix != null) {
          ctx.write(" ").write(fkeySuffix);
        }
      }
			ctx.write(";").writeNewLine();

			if (addMode && ddlSyntax.isRenderIndexForFkey()){

				//create index idx_fk_o_address_ctry on o_address(country_code);
				ctx.write("create index ");

				String idxName = ddlSyntax.getIndexName(tableName, p.getName(), ctx.incrementIxCount());
				if (idxName != null){
					ctx.write(idxName);
				}

				ctx.write(" on ").write(baseTable).write(" (");
				for (int i = 0; i < columns.length; i++) {
					if (i > 0){
						ctx.write(",");
					}
					ctx.write(columns[i].getLocalDbColumn());
				}
				ctx.write(");").writeNewLine();
			}
		}

    /**
     * Return 'add' or 'drop' of foreign key.
     */
    protected String addOrDrop() {
      return addMode ? " add " : " drop " ;
    }

    @Override
    public void visitScalar(BeanProperty p) {
      // not interested
    }

    @Override
    public void visitCompound(BeanPropertyCompound p) {
      // not interested
    }

    @Override
    public void visitCompoundScalar(BeanPropertyCompound compound, BeanProperty p) {
      // not interested
    }
  }

}
