package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContextUtil;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne imported reference but with inheritance.
 */
class AssocOneHelpRefInherit extends AssocOneHelp {

  private final InheritInfo inherit;

  AssocOneHelpRefInherit(BeanPropertyAssocOne property) {
    super(property);
    this.inherit = property.targetInheritInfo;
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    property.targetIdBinder.loadIgnore(ctx);
    ctx.getDataReader().incrementPos(1);
  }

  /**
   * Read and set a Reference bean.
   */
  @Override
  Object read(DbReadContext ctx) throws SQLException {

    // read discriminator to determine the type
    InheritInfo rowInheritInfo = inherit.readType(ctx);
    if (rowInheritInfo == null) {
      return null;
    }
    BeanDescriptor<?> desc = rowInheritInfo.getBeanDescriptor();

    // read the foreign key column(s)
    Object id = property.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }

    // check transaction context to see if it already exists
    Class<?>  rowType = desc.rootBeanType;
    Object existing = ctx.getPersistenceContext().get(rowType, id);
    if (existing != null) {
      return existing;
    }

    // for inheritance hierarchy create the correct type for this row...
    Object  ref = desc.createReference(ctx.isReadOnly(), id);

    Class<?> rootType = PersistenceContextUtil.root(ref.getClass());
    ctx.getPersistenceContext().put(rootType, id, ref);

    EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
    ctx.register(property.name, ebi);

    return ref;
  }

  @Override
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {

    // add join to support the discriminator column
    String relativePrefix = ctx.getRelativePrefix(property.name);
    property.tableJoin.addJoin(joinType, relativePrefix, ctx);
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {

    if (!subQuery) {
      // add discriminator column
      String relativePrefix = ctx.getRelativePrefix(property.getName());
      String tableAlias = ctx.getTableAlias(relativePrefix);
      ctx.appendColumn(tableAlias, property.targetInheritInfo.getDiscriminatorColumn());
    }
    property.importedId.sqlAppend(ctx);
  }
}
