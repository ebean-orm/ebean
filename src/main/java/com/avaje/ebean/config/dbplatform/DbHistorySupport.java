package com.avaje.ebean.config.dbplatform;

/**
 * History support for the database platform.
 */
public interface DbHistorySupport {

  /**
   * Return the number of columns bound in a 'As Of' predicate.
   * <p>
   * Typically this is 2 but 1 for postgres using it's range type.
   * </p>
   */
  int getBindCount();

  /**
   * Return the 'as of' predicate added for the given table alias.
   *
   * @param tableAlias The table alias this predicate is added for
   * @param sysPeriod  The name of the 'sys_period' column used for effective date time range.
   * @return The predicate containing a single ? bind parameter which will be bound to the 'as at' timestamp value
   */
  String getAsOfPredicate(String tableAlias, String sysPeriod);

  /**
   * Return the column for the system period lower bound that will be included in findVersions() queries.
   *
   * @param tableAlias the table alias which will typically be 't0'
   * @param sysPeriod the name of the sys_period column
   */
  String getSysPeriodLower(String tableAlias, String sysPeriod);

  /**
   * Return the column for the system period upper bound that will be included in findVersions() queries.
   *
   * @param tableAlias the table alias which will typically be 't0'
   * @param sysPeriod the name of the sys_period column
   */
  String getSysPeriodUpper(String tableAlias, String sysPeriod);

}
