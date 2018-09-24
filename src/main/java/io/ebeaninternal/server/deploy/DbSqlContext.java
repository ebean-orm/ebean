package io.ebeaninternal.server.deploy;

import io.ebeaninternal.server.query.SqlJoinType;

/**
 * Used to provide context during sql construction.
 */
public interface DbSqlContext {

  /**
   * Add a join to the sql query.
   */
  void addJoin(String type, String table, TableJoinColumn[] cols, String a1, String a2, String inheritance);

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
  BeanProperty[] getEncryptedProps();

  /**
   * Append a char directly to the SQL buffer.
   */
  DbSqlContext append(char s);

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
  void appendRawColumn(String rawcolumnWithTableAlias);

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
  void appendFormulaJoin(String sqlFormulaJoin, SqlJoinType joinType);

  /**
   * Return the current content length.
   */
  int length();

  /**
   * Return the current context of the sql context.
   */
  String getContent();

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
  String getTableAlias(String prefix);

  /**
   * Return a table alias that takes into account many where joins.
   */
  String getTableAliasManyWhere(String prefix);

  String getRelativePrefix(String propName);

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

}
