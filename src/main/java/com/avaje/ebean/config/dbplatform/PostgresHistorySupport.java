package com.avaje.ebean.config.dbplatform;

/**
 * Postgres support for history features.
 */
public class PostgresHistorySupport extends DbViewHistorySupport {

  /**
   * Return 1 as we are using the postgres range type and hence don't need 2 bind variables.
   */
  @Override
  public int getBindCount() {
    return 1;
  }

  /**
   * Build and return the 'as of' predicate for a given table alias.
   * <p>
   * Each @History entity involved in the query has this predicate added using the related table alias.
   * </p>
   */
  @Override
  public String getAsOfPredicate(String asOfTableAlias, String asOfSysPeriod) {

    // for Postgres we are using the 'timestamp with timezone range' data type
    // as our sys_period column so hence the predicate below
    return asOfTableAlias + "." + asOfSysPeriod + " @> ?::timestamptz";
  }

  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return "lower(" + tableAlias + "." + sysPeriod + ")";
  }

  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return "upper(" + tableAlias + "." + sysPeriod + ")";
  }
}
