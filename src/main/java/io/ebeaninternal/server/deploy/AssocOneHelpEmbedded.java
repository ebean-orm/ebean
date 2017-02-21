package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;

import java.sql.SQLException;

/**
 * Helper for Embedded BeanPropertyAssocOne.
 */
final class AssocOneHelpEmbedded extends AssocOneHelp {

  public AssocOneHelpEmbedded(BeanPropertyAssocOne<?> property) {
    super(property);
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    for (int i = 0; i < property.embeddedProps.length; i++) {
      property.embeddedProps[i].loadIgnore(ctx);
    }
  }

  @Override
  Object readSet(DbReadContext ctx, EntityBean bean) throws SQLException {
    Object dbVal = read(ctx);
    if (bean != null) {
      // set back to the parent bean
      property.setValue(bean, dbVal);
      ctx.propagateState(dbVal);
      return dbVal;
    } else {
      return null;
    }
  }

  @Override
  Object read(DbReadContext ctx) throws SQLException {

    EntityBean embeddedBean = property.targetDescriptor.createEntityBean();

    boolean notNull = false;
    for (int i = 0; i < property.embeddedProps.length; i++) {
      Object value = property.embeddedProps[i].readSet(ctx, embeddedBean);
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
  void appendSelect(DbSqlContext ctx, boolean subQuery) {
    for (int i = 0; i < property.embeddedProps.length; i++) {
      property.embeddedProps[i].appendSelect(ctx, subQuery);
    }
  }
}
