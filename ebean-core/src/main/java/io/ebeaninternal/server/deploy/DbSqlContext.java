package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlJoinType;
import io.ebeaninternal.server.query.SqlTreeJoin;

/**
 * Used to provide context during sql construction.
 */
public interface DbSqlContext {

  /**
   * Add a join to the sql query.
   */
  void addJoin(String type, String table, TableJoinColumn[] cols, String a1, String a2, String extraWhere);

  /**
   * Push the current table alias onto the stack.
   */
  void pushTableAlias(String tableAlias);

  /**
   * Pop the current table alias from the stack.
   */
  void popTableAlias();

  /**
   * Add an encrypted property which will require additional binding.
   */
  void addEncryptedProp(BeanProperty prop);

  /**
   * Return a list of encrypted properties which require additional binding.
   */
  BeanProperty[] encryptedProps();

  /**
   * Append a string directly to the SQL buffer.
   */
  DbSqlContext append(String s);

  /**
   * Peek the current table alias.
   */
  String peekTableAlias();

  /**
   * Add a raw column to the sql.
   */
  void appendRawColumn(String rawColumnWithTableAlias);

  /**
   * Append a column with an explicit table alias.
   */
  void appendColumn(String tableAlias, String column);

  /**
   * Append a column with the current table alias.
   */
  void appendColumn(String column);

  /**
   * Parse and add formula with standard table alias replacement.
   */
  void appendParseSelect(String parseSelect, String alias);

  /**
   * Append a Sql Formula select. This converts the "${ta}" keyword to the
   * current table alias.
   */
  void appendFormulaSelect(String sqlFormulaSelect);

  /**
   * Append a Sql Formula join. This converts the "${ta}" keyword to the current
   * table alias.
   */
  void appendFormulaJoin(String sqlFormulaJoin, SqlJoinType joinType, String tableAlias);

  /**
   * Return the current content length.
   */
  int length();

  /**
   * Return the current context of the sql context.
   */
  String content();

  /**
   * Push a join node onto the stack.
   */
  void pushJoin(String prefix);

  /**
   * Pop a join node off the stack.
   */
  void popJoin();

  /**
   * Return a table alias without many where clause joins. Typically this is for
   * the select clause (fetch joins).
   */
  String tableAlias(String prefix);

  /**
   * Return a table alias that takes into account many where joins.
   */
  String tableAliasManyWhere(String prefix);

  String relativePrefix(String propName);

  /**
   * Append the lower and upper bound columns into the select clause
   * for findVersions() queries.
   */
  void appendHistorySysPeriod();

  /**
   * Return true if the query includes soft deleted rows.
   */
  boolean isIncludeSoftDelete();

  /**
   * Return the count of history table AS OF predicates added via joins.
   */
  int asOfTableCount();

  /**
   * Return true if the query is a 'asDraft' query.
   */
  boolean isDraftQuery();

  /**
   * Start group by clause.
   */
  void startGroupBy();

  /**
   * Append 'for update' lock hints on FROM clause (sql server only).
   */
  void appendFromForUpdate();

  /**
   * Delay adding an extra join to support inheritance discriminator in projection (IF required).
   */
  void addExtraJoin(SqlTreeJoin treeJoin);

  /**
   * Add extra joins *IF* required to support inheritance discriminator in projection.
   */
  void flushExtraJoins();

  /**
   * Return true if the last join was added and false means the join was suppressed
   * as it was already added to the query.
   */
  boolean joinAdded();

  /**
   * Include the filter many predicates if specified into the JOIN clause.
   */
  void includeFilterMany();
}
