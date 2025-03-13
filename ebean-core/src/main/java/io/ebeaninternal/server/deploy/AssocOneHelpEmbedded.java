package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.core.type.DataReader;

import java.sql.SQLException;

/**
 * Helper for Embedded BeanPropertyAssocOne.
 */
final class AssocOneHelpEmbedded extends AssocOneHelp {

  AssocOneHelpEmbedded(BeanPropertyAssocOne<?> property) {
    super(property);
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    for (BeanProperty property : property.embeddedProps) {
      property.loadIgnore(ctx);
    }
  }

  @Override
  Object readSet(DataReader reader, EntityBean bean) throws SQLException {
    Object dbVal = read(reader);
    if (bean != null) {
      property.setValue(bean, dbVal);
    }
    return dbVal;
  }

  @Override
  Object read(DataReader reader) throws SQLException {
    EntityBean embeddedBean = property.targetDescriptor.createEntityBean2(reader.unmodifiable());
    boolean notNull = false;
    for (BeanProperty property : property.embeddedProps) {
      Object value = property.readSet(reader, embeddedBean);
      if (value != null) {
        notNull = true;
      }
    }
    if (notNull) {
      return embeddedBean;
    } else {
      return null;
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
    EntityBean embeddedBean = property.targetDescriptor.createEntityBean2(ctx.unmodifiable());
    boolean notNull = false;
    for (BeanProperty property : property.embeddedProps) {
      Object value = property.readSet(ctx, embeddedBean);
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
    for (BeanProperty property : property.embeddedProps) {
      property.appendSelect(ctx, subQuery);
    }
  }
}
