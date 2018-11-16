package io.ebean.config.dbplatform.hana;

import io.ebean.config.dbplatform.DbStandardHistorySupport;

public class HanaHistorySupport extends DbStandardHistorySupport {

  @Override
  public String getAsOfViewSuffix(String asOfViewSuffix) {
    return " for system_time as of ?";
  }

  @Override
  public String getVersionsBetweenSuffix(String asOfViewSuffix) {
    return " for system_time between ? and ?";
  }

  @Override
  public String getSysPeriodLower(String tableAlias, String sysPeriod) {
    return tableAlias + "." + sysPeriod + "_start";
  }

  @Override
  public String getSysPeriodUpper(String tableAlias, String sysPeriod) {
    return tableAlias + "." + sysPeriod + "_end";
  }
  
}
