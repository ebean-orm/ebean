package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlJoinType;

/**
 * Helper for BeanPropertyAssocOne for OneToOne exported reference - not so common.
 */
class AssocOneHelpRefExported extends AssocOneHelp {

  public AssocOneHelpRefExported(BeanPropertyAssocOne<?> property) {
    super(property);
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
    property.tableJoin.addJoin(joinType, relativePrefix, ctx);
  }
}
