package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne for OneToOne exported reference - not so common.
 */
final class AssocOneHelpReferenceExported extends AssocOneHelp {

  public AssocOneHelpReferenceExported(BeanPropertyAssocOne property) {
    super(property);
  }

  @Override
  Object read(DbReadContext ctx) throws SQLException {

    // Support for Inheritance hierarchy on exported OneToOne ?
    Object id = property.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }

    PersistenceContext pc = ctx.getPersistenceContext();
    Object existing = pc.get(property.targetType, id);
    if (existing != null) {
      return existing;
    }

    Object ref = property.targetDescriptor.createReference(ctx.isReadOnly(), id);
    property.targetDescriptor.contextPut(pc, id, ref);

    EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
    ctx.register(property.name, ebi);
    return ref;
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {

    // set appropriate tableAlias for the exported id columns
    String relativePrefix = ctx.getRelativePrefix(property.getName());
    ctx.pushTableAlias(relativePrefix);
    property.targetIdBinder.appendSelect(ctx, subQuery);
    ctx.popTableAlias();
  }

  @Override
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {

    String relativePrefix = ctx.getRelativePrefix(property.getName());
    property.tableJoin.addJoin(joinType, relativePrefix, ctx);
  }
}
