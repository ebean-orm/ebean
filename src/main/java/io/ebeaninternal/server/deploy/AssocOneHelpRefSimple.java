package io.ebeaninternal.server.deploy;

/**
 * Helper for BeanPropertyAssocOne imported reference - this is the common case.
 */
class AssocOneHelpRefSimple extends AssocOneHelp {

  AssocOneHelpRefSimple(BeanPropertyAssocOne<?> property) {
    super(property);
  }

  /**
   * Append columns for foreign key columns.
   */
  @Override
  void appendSelect(DbSqlContext ctx, boolean subQuery) {
    property.importedId.sqlAppend(ctx);
  }
}
