package com.avaje.ebean.config.dbplatform;

/**
 * Runtime support for @History with MySql.
 */
public class MySqlHistorySupport implements DbHistorySupport {

  @Override
  public int getBindCount() {
    return 2;
  }

  @Override
  public String getAsOfPredicate(String asOfTableAlias, String asOfSysPeriod) {

    StringBuilder sb = new StringBuilder(90);
    sb.append("(");
    sb.append(asOfTableAlias).append(".").append(asOfSysPeriod).append("_start").append(" < ? and (");
    sb.append(asOfTableAlias).append(".").append(asOfSysPeriod).append("_end").append(" is null or ");
    sb.append(asOfTableAlias).append(".").append(asOfSysPeriod).append("_end").append(" > ?))");

    // (sys_period_start < ? and (sys_period_end is null or sys_period_end > ?));
    return sb.toString();
  }

  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return tableAlias+"."+sysPeriod+"_start";
  }

  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return tableAlias+"."+sysPeriod+"_end";
  }
}
