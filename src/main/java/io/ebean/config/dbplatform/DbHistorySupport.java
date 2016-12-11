package io.ebean.config.dbplatform;

/**
 * History support for the database platform.
 */
public interface DbHistorySupport {

  /**
   * Return true if the implementation is SQL2011 standards based.
   * <p>
   * Non standards based means we need to add additional predicates into the
   * JOIN ON clause and add an additional predicate for the base table.
   * </p>
   */
  boolean isStandardsBased();

  /**
   * Return the number of columns bound in a 'As Of' predicate.
   * <p>
   * This is 1 for more standard sql2011 style and Postgres which has the
   * special range type and 2 for view based solutions with 2 columns such as
   * MySql.
   * </p>
   */
  int getBindCount();

  /**
   * For sql2011 style this ignores the passed in view suffix and returns something
   * like the ' as of timestamp ?' clause to be appended after the base table name.
   *
   * @param asOfViewSuffix the configured view suffix (typically "_with_history").
   * @return The suffix appended after the base table name in the from and join clauses.
   */
  String getAsOfViewSuffix(String asOfViewSuffix);

  /**
   * Return the 'versions between timestamp' suffix.
   */
  String getVersionsBetweenSuffix(String asOfViewSuffix);

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
   * @param sysPeriod  the name of the sys_period column
   */
  String getSysPeriodLower(String tableAlias, String sysPeriod);

  /**
   * Return the column for the system period upper bound that will be included in findVersions() queries.
   *
   * @param tableAlias the table alias which will typically be 't0'
   * @param sysPeriod  the name of the sys_period column
   */
  String getSysPeriodUpper(String tableAlias, String sysPeriod);

}
