package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Local interface to handle Embedded, Reference and Reference Exported
 * cases.
 */
abstract class AssocOneHelp {

  protected final BeanPropertyAssocOne property;

  AssocOneHelp(BeanPropertyAssocOne property) {
    this.property = property;
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
  abstract Object read(DbReadContext ctx) throws SQLException;

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
