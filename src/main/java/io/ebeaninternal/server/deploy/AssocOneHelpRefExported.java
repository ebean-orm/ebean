package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlJoinType;

/**
 * Helper for BeanPropertyAssocOne for OneToOne exported reference - not so common.
 */
class AssocOneHelpRefExported extends AssocOneHelp {

  private final boolean softDelete;

  private final String softDeletePredicate;

  AssocOneHelpRefExported(BeanPropertyAssocOne<?> property) {
    super(property);
    this.softDelete = property.targetDescriptor.isSoftDelete();
    this.softDeletePredicate = (softDelete) ? property.targetDescriptor.getSoftDeletePredicate("") : null;
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
    if (softDelete && !ctx.isIncludeSoftDelete()) {
      property.tableJoin.addJoin(joinType, relativePrefix, ctx, softDeletePredicate);
    } else {
      property.tableJoin.addJoin(joinType, relativePrefix, ctx);
    }
  }
}
