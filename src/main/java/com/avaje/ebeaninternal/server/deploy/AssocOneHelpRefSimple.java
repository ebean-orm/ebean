package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne imported reference - this is the common case.
 */
class AssocOneHelpRefSimple extends AssocOneHelp {

  private final BeanDescriptor target;

  AssocOneHelpRefSimple(BeanPropertyAssocOne property) {
    super(property);
    this.target = property.targetDescriptor;
  }

  /**
   * Read and set a Reference bean.
   */
  @Override
  Object read(DbReadContext ctx) throws SQLException {

    // read the foreign key column(s)
    Object id = property.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }

    Class<?> rowType = property.targetType;

    // check transaction context to see if it already exists
    Object existing = ctx.getPersistenceContext().get(rowType, id);
    if (existing != null) {
      return existing;
    }

    Object ref = target.createReference(ctx.isReadOnly(), id);
    target.contextPut(ctx.getPersistenceContext(), id, ref);

    EntityBeanIntercept ebi = ((EntityBean) ref)._ebean_getIntercept();
    ctx.register(property.name, ebi);

    return ref;
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {
    property.importedId.sqlAppend(ctx);
  }
}
