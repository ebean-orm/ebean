package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Local interface to handle Embedded, Reference and Reference Exported
 * cases.
 */
abstract class AssocOneHelp {

  protected final BeanPropertyAssocOne<?> property;

  protected final BeanDescriptor<?> target;

  AssocOneHelp(BeanPropertyAssocOne<?> property) {
    this.property = property;
    this.target = property.targetDescriptor;
  }

  /**
   * Effectively skip reading (the jdbc resultSet as already in the persistence context etc).
   */
  void loadIgnore(DbReadContext ctx) {
    property.targetIdBinder.loadIgnore(ctx);
  }

  /**
   * Read and return the bean.
   */
  Object read(DbReadContext ctx) throws SQLException {

    // Support for Inheritance hierarchy on exported OneToOne ?
    Object id = property.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }

    PersistenceContext pc = ctx.getPersistenceContext();
    Object existing = target.contextGet(pc, id);
    if (existing != null) {
      return existing;
    }

    boolean disableLazyLoading = ctx.isDisableLazyLoading();
    Object ref = target.contextRef(pc, ctx.isReadOnly(), disableLazyLoading, id);
    if (!disableLazyLoading) {
      ctx.register(property.name, ((EntityBean) ref)._ebean_getIntercept());
    }
    return ref;
  }


  /**
   * Read setting values into the bean.
   */
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object val = read(ctx);
    if (bean != null) {
      property.setValue(bean, val);
      ctx.propagateState(val);
    }
    return val;
  }

  /**
   * Append to the select clause.
   */
  abstract void appendSelect(DbSqlContext ctx, boolean subQuery);

  /**
   * Append to the from clause.
   */
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    // nothing required here
  }

}
