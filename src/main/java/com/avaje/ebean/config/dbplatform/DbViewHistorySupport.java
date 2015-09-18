package com.avaje.ebean.config.dbplatform;

/**
 * Database view based implementation of DbHistorySupport.
 * <p>
 * These implementations have explicit history tables, a view to union the
 * base table and history table and triggers to maintain the history.
 * </p>
 */
public abstract class DbViewHistorySupport implements DbHistorySupport {

  /**
   * Return false for view based implementations where we append extra 'as of' predicates to the end.
   */
  @Override
  public boolean isBindWithFromClause() {
    return false;
  }

  /**
   * Returns the configured view suffix.
   *
   * @param asOfViewSuffix the configured view suffix (typically "_with_history").
   */
  @Override
  public String getAsOfViewSuffix(String asOfViewSuffix) {
    // just return the configured suffix
    return asOfViewSuffix;
  }

  /**
   * Returns the configured view suffix (same as getAsOfViewSuffix()).
   *
   * @param asOfViewSuffix the configured view suffix (typically "_with_history").
   */
  @Override
  public String getVersionsBetweenSuffix(String asOfViewSuffix) {
    // just return the configured asOfViewSuffix (using the view for versions between query)
    return asOfViewSuffix;
  }

  /**
   * Return 2 if we have effective start and effective end as 2 columns.
   * Note that for postgres we can use a single range type so that returns 1.
   */
  @Override
  public int getBindCount() {
    return 2;
  }

  /**
   * Return the 'as of' predicate clause appended to the end of the normal query predicates.
   */
  @Override
  public String getAsOfPredicate(String asOfTableAlias, String asOfSysPeriod) {

    // (sys_period_start < ? and (sys_period_end is null or sys_period_end > ?));
    return "(" + asOfTableAlias + "." + asOfSysPeriod + "_start" + " <= ? and (" + asOfTableAlias + "." + asOfSysPeriod + "_end" + " is null or " + asOfTableAlias + "." + asOfSysPeriod + "_end" + " > ?))";
  }

  /**
   * Return the lower bound column prepended with the table alias.
   *
   * @param tableAlias the table alias
   * @param sysPeriod  the name of the sys_period column
   */
  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return tableAlias + "." + sysPeriod + "_start";
  }

  /**
   * Return the upper bound column prepended with the table alias.
   *
   * @param tableAlias the table alias
   * @param sysPeriod  the name of the sys_period column
   */
  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return tableAlias + "." + sysPeriod + "_end";
  }
}
