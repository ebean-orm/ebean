package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.server.query.SqlJoinType;

/**
 * Used to provide context during sql construction.
 */
public interface DbSqlContext {

  /**
   * Add a join to the sql query.
   */
  public void addJoin(String type, String table, TableJoinColumn[] cols, String a1, String a2, String inheritance);

  public void pushSecondaryTableAlias(String alias);

  /**
   * Push the current table alias onto the stack.
   */
  public void pushTableAlias(String tableAlias);

  /**
   * Pop the current table alias from the stack.
   */
  public void popTableAlias();

  /**
   * Add an encrypted property which will require additional binding.
   */
  public void addEncryptedProp(BeanProperty prop);

  /**
   * Return a list of encrypted properties which require additional binding.
   */
  public BeanProperty[] getEncryptedProps();

  /**
   * Append a char directly to the SQL buffer.
   */
  public DbSqlContext append(char s);

  /**
   * Append a string directly to the SQL buffer.
   */
  public DbSqlContext append(String s);

  /**
   * Peek the current table alias.
   */
  public String peekTableAlias();

  /**
   * Add a raw column to the sql.
   */
  public void appendRawColumn(String rawcolumnWithTableAlias);

  /**
   * Append a column with an explicit table alias.
   */
  public void appendColumn(String tableAlias, String column);

  /**
   * Append a column with the current table alias.
   */
  public void appendColumn(String column);

  /**
   * Append a Sql Formula select. This converts the "${ta}" keyword to the
   * current table alias.
   */
  public void appendFormulaSelect(String sqlFormulaSelect);

  /**
   * Append a Sql Formula join. This converts the "${ta}" keyword to the current
   * table alias.
   */
  public void appendFormulaJoin(String sqlFormulaJoin, SqlJoinType joinType);

  /**
   * Return the current content length.
   */
  public int length();

  /**
   * Return the current context of the sql context.
   */
  public String getContent();

  /**
   * Return the current join node.
   */
  public String peekJoin();

  /**
   * Push a join node onto the stack.
   */
  public void pushJoin(String prefix);

  /**
   * Pop a join node off the stack.
   */
  public void popJoin();

  /**
   * Return a table alias without many where clause joins. Typically this is for
   * the select clause (fetch joins).
   */
  public String getTableAlias(String prefix);

  /**
   * Return a table alias that takes into account many where joins.
   */
  public String getTableAliasManyWhere(String prefix);

  public String getRelativePrefix(String propName);

}
