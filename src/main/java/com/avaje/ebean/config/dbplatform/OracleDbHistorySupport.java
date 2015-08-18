package com.avaje.ebean.config.dbplatform;

/**
 * Oracle Total recall based history support.
 */
public class OracleDbHistorySupport extends DbStandardHistorySupport {

  /**
   * Return the ' as of timestamp ?' clause appended after the table name.
   */
  @Override
  public String getAsOfViewSuffix(String asOfViewSuffix) {
    return " as of TIMESTAMP ?";
  }

  @Override
  public String getVersionsBetweenSuffix(String asOfViewSuffix) {
    return " versions between timestamp ? and ?";
  }

  /**
   * Returns the Oracle specific effective start column.
   */
  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return "versions_starttime";
  }

  /**
   * Returns the Oracle specific effective end column.
   */
  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return "versions_endtime";
  }

}
