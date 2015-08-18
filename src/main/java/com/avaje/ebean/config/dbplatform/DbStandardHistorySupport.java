package com.avaje.ebean.config.dbplatform;

/**
 * SQL2011 based history support using 'as of timestamp' type clause appended as part of the the from or join clause.
 */
public abstract class DbStandardHistorySupport implements DbHistorySupport {

  /**
   * Return true as with sql2011 the 'as of timestamp' clause included in from or join clause.
   */
  @Override
  public boolean isBindWithFromClause() {
    return true;
  }

  /**
   * Return 1 as the bind count (not 2 for effective start and effective end columns).
   */
  @Override
  public int getBindCount() {
    return 1;
  }

  /**
   * Return null - not used for sql2011 based history.
   */
  @Override
  public String getAsOfPredicate(String tableAlias, String sysPeriod) {
    // not used for sql2011 based history
    return null;
  }
}
