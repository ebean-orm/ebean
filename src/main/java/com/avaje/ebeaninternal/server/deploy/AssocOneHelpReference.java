package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.bean.PersistenceContextUtil;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne imported reference - this is the common case.
 */
final class AssocOneHelpReference extends AssocOneHelp {

  private BeanPropertyAssocOne beanPropertyAssocOne;

  AssocOneHelpReference(BeanPropertyAssocOne beanPropertyAssocOne) {
    this.beanPropertyAssocOne = beanPropertyAssocOne;
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    beanPropertyAssocOne.targetIdBinder.loadIgnore(ctx);
    if (beanPropertyAssocOne.targetInheritInfo != null) {
      ctx.getDataReader().incrementPos(1);
    }
  }

  @Override
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object val = read(ctx);
    if (bean != null) {
      beanPropertyAssocOne.setValue(bean, val);
      ctx.propagateState(val);
    }
    return val;
  }

  /**
   * Read and set a Reference bean.
   */
  @Override
  Object read(DbReadContext ctx) throws SQLException {

    BeanDescriptor<?> rowDescriptor = null;
    Class<?> rowType = beanPropertyAssocOne.targetType;
    if (beanPropertyAssocOne.targetInheritInfo != null) {
      // read discriminator to determine the type
      InheritInfo rowInheritInfo = beanPropertyAssocOne.targetInheritInfo.readType(ctx);
      if (rowInheritInfo != null) {
        rowType = rowInheritInfo.getType();
        rowDescriptor = rowInheritInfo.getBeanDescriptor();
      }
    }

    // read the foreign key column(s)
    Object id = beanPropertyAssocOne.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }

    // check transaction context to see if it already exists
    Object existing = ctx.getPersistenceContext().get(rowType, id);

    if (existing != null) {
      return existing;
    }

    Boolean readOnly = ctx.isReadOnly();
    Object ref;
    if (beanPropertyAssocOne.targetInheritInfo != null) {
      // for inheritance hierarchy create the correct type for this row...
      ref = rowDescriptor.createReference(readOnly, id);
    } else {
      ref = beanPropertyAssocOne.targetDescriptor.createReference(readOnly, id);
    }

    Class<?> rootType = PersistenceContextUtil.root(ref.getClass());
    Object existingBean = ctx.getPersistenceContext().putIfAbsent(rootType, id, ref);
    if (existingBean != null) {
      // advanced case when we use multiple concurrent threads to
      // build a single object graph, and another thread has since
      // loaded a matching bean so we will use that instead.
      ref = existingBean;

    } else {
      EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
      if (Boolean.TRUE.equals(ctx.isReadOnly())) {
        ebi.setReadOnly(true);
      }
      ctx.register(beanPropertyAssocOne.name, ebi);
    }

    return ref;
  }

  @Override
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    if (beanPropertyAssocOne.targetInheritInfo != null) {
      // add join to support the discriminator column
      String relativePrefix = ctx.getRelativePrefix(beanPropertyAssocOne.name);
      beanPropertyAssocOne.tableJoin.addJoin(joinType, relativePrefix, ctx);
    }
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {

    if (!subQuery && beanPropertyAssocOne.targetInheritInfo != null) {
      // add discriminator column
      String relativePrefix = ctx.getRelativePrefix(beanPropertyAssocOne.getName());
      String tableAlias = ctx.getTableAlias(relativePrefix);
      ctx.appendColumn(tableAlias, beanPropertyAssocOne.targetInheritInfo.getDiscriminatorColumn());
    }
    beanPropertyAssocOne.importedId.sqlAppend(ctx);
  }
}
