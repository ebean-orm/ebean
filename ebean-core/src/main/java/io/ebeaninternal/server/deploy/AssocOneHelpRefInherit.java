package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.query.SqlJoinType;
import io.ebeaninternal.server.query.SqlTreeJoin;

import java.sql.SQLException;

/**
 * Helper for BeanPropertyAssocOne imported reference but with inheritance.
 */
final class AssocOneHelpRefInherit extends AssocOneHelp {

  private final InheritInfo inherit;

  AssocOneHelpRefInherit(BeanPropertyAssocOne<?> property) {
    super(property);
    this.inherit = property.targetInheritInfo;
  }

  @Override
  void loadIgnore(DbReadContext ctx) {
    property.targetIdBinder.loadIgnore(ctx);
    ctx.dataReader().incrementPos(1);
  }

  /**
   * Read and set a Reference bean.
   */
  @Override
  Object read(DbReadContext ctx) throws SQLException {
    // read discriminator to determine the type
    InheritInfo rowInheritInfo = inherit.readType(ctx);
    BeanDescriptor<?> desc;
    if (rowInheritInfo != null) {
      desc = rowInheritInfo.desc();
    } else if (!inherit.hasChildren()) {
      desc = inherit.desc();
    } else {
      // ignore the id property
      property.targetIdBinder.loadIgnore(ctx);
      return null;
    }
    Object id = property.targetIdBinder.read(ctx);
    if (id == null) {
      return null;
    }
    // check transaction context to see if it already exists
    PersistenceContext pc = ctx.persistenceContext();
    Object existing = desc.contextGet(pc, id);
    if (existing != null) {
      return existing;
    }
    // for inheritance hierarchy create the correct type for this row...
    Object ref = desc.contextRef(pc, id, ctx.unmodifiable(), ctx.isDisableLazyLoading());
    if (!ctx.unmodifiable() && !ctx.isDisableLazyLoading()) {
      ctx.registerBeanInherit(property, ((EntityBean) ref)._ebean_getIntercept());
    }
    return ref;
  }

  void appendFrom(DbSqlContext ctx, SqlJoinType joinType) {
    // add join to support the discriminator column
    String relativePrefix = ctx.relativePrefix(property.name);
    ctx.addExtraJoin(new Extra(relativePrefix, joinType));
  }

  /**
   * Extra join to support the discriminator column.
   */
  final class Extra implements SqlTreeJoin {
    final String relativePrefix;
    final SqlJoinType joinType;
    Extra(String relativePrefix, SqlJoinType joinType) {
      this.relativePrefix = relativePrefix;
      this.joinType = joinType;
    }

    @Override
    public void addJoin(DbSqlContext ctx) {
      // add join to support the discriminator column *IF* join is not already present
      property.tableJoin.addJoin(joinType, relativePrefix, ctx);
    }
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {
    if (!subQuery) {
      // add discriminator column
      String relativePrefix = ctx.relativePrefix(property.name());
      String tableAlias = ctx.tableAlias(relativePrefix);
      ctx.appendColumn(tableAlias, property.targetInheritInfo.getDiscriminatorColumn());
    }
    property.importedId.sqlAppend(ctx);
  }
}
