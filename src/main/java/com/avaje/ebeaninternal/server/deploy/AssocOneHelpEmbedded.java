package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.server.query.SqlJoinType;

import java.sql.SQLException;

/**
 * Helper for Embedded BeanPropertyAssocOne.
 */
final class AssocOneHelpEmbedded extends AssocOneHelp {

  private BeanPropertyAssocOne beanPropertyAssocOne;

  public AssocOneHelpEmbedded(BeanPropertyAssocOne beanPropertyAssocOne) {
    this.beanPropertyAssocOne = beanPropertyAssocOne;
  }

  void loadIgnore(DbReadContext ctx) {
    for (int i = 0; i < beanPropertyAssocOne.embeddedProps.length; i++) {
      beanPropertyAssocOne.embeddedProps[i].loadIgnore(ctx);
    }
  }

  @Override
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object dbVal = read(ctx);
    if (bean != null) {
      // set back to the parent bean
      beanPropertyAssocOne.setValue(bean, dbVal);
      ctx.propagateState(dbVal);
      return dbVal;

    } else {
      return null;
    }
  }

  @Override
  Object read(DbReadContext ctx) throws SQLException {

    EntityBean embeddedBean = beanPropertyAssocOne.targetDescriptor.createEntityBean();

    boolean notNull = false;
    for (int i = 0; i < beanPropertyAssocOne.embeddedProps.length; i++) {
      Object value = beanPropertyAssocOne.embeddedProps[i].readSet(ctx, embeddedBean);
      if (value != null) {
        notNull = true;
      }
    }
    if (notNull) {
      ctx.propagateState(embeddedBean);
      return embeddedBean;
    } else {
      return null;
    }
  }

  @Override
  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
  }

  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {
    for (int i = 0; i < beanPropertyAssocOne.embeddedProps.length; i++) {
      beanPropertyAssocOne.embeddedProps[i].appendSelect(ctx, subQuery);
    }
  }
}
