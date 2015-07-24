package com.avaje.ebean.config.dbplatform;

/**
 * Postgres support for history features.
 */
public class PostgresHistorySupport implements DbHistorySupport {

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
    StringBuilder sb = new StringBuilder(40);
    sb.append(asOfTableAlias).append(".").append(asOfSysPeriod).append(" @> ?::timestamptz");
    return sb.toString();
  }

  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return "lower("+tableAlias+"."+sysPeriod+")";
  }

  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return "upper("+tableAlias+"."+sysPeriod+")";
  }
}
