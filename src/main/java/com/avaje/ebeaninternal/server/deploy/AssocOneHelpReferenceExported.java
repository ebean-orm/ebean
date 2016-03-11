package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.id.IdBinder;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne for OneToOne exported reference - not so common.
 */
final class AssocOneHelpReferenceExported extends AssocOneHelp {

  private BeanPropertyAssocOne beanPropertyAssocOne;

  public AssocOneHelpReferenceExported(BeanPropertyAssocOne beanPropertyAssocOne) {
    this.beanPropertyAssocOne = beanPropertyAssocOne;
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    beanPropertyAssocOne.targetDescriptor.getIdBinder().loadIgnore(ctx);
  }

  /**
   * Read and set a Reference bean.
   */
  @Override
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {

    Object dbVal = read(ctx);
    if (bean != null) {
      beanPropertyAssocOne.setValue(bean, dbVal);
      ctx.propagateState(dbVal);
    }
    return dbVal;
  }

  @Override
  Object read(DbReadContext ctx) throws SQLException {

    // TODO: Support for Inheritance hierarchy on exported OneToOne ?
    IdBinder idBinder = beanPropertyAssocOne.targetDescriptor.getIdBinder();
    Object id = idBinder.read(ctx);
    if (id == null) {
      return null;
    }

    PersistenceContext persistCtx = ctx.getPersistenceContext();
    Object existing = persistCtx.get(beanPropertyAssocOne.targetType, id);

    if (existing != null) {
      return existing;
    }
    Object ref = beanPropertyAssocOne.targetDescriptor.createReference(ctx.isReadOnly(), id);

    EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
    if (Boolean.TRUE.equals(ctx.isReadOnly())) {
      ebi.setReadOnly(true);
    }
    beanPropertyAssocOne.targetDescriptor.contextPut(persistCtx, id, ref);
    ctx.register(beanPropertyAssocOne.name, ebi);
    return ref;
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {

    // set appropriate tableAlias for the exported id columns

    String relativePrefix = ctx.getRelativePrefix(beanPropertyAssocOne.getName());
    ctx.pushTableAlias(relativePrefix);

    IdBinder idBinder = beanPropertyAssocOne.targetDescriptor.getIdBinder();
    idBinder.appendSelect(ctx, subQuery);

    ctx.popTableAlias();
  }

  @Override
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {

    String relativePrefix = ctx.getRelativePrefix(beanPropertyAssocOne.getName());
    beanPropertyAssocOne.tableJoin.addJoin(joinType, relativePrefix, ctx);
  }
}
