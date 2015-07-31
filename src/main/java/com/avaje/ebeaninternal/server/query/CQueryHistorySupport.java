package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.config.dbplatform.DbHistorySupport;

import java.util.Map;

/**
 * Helper to support history functions.
 */
public class CQueryHistorySupport {

  /**
   * The DB specific support.
   */
  private final DbHistorySupport dbHistorySupport;

  /**
   * The mapping of base tables to their matching 'with history' views.
   */
  private final Map<String, String> asOfTableMap;

  /**
   * The sys period column.
   */
  private final String sysPeriod;

  public CQueryHistorySupport(DbHistorySupport dbHistorySupport, Map<String, String> asOfTableMap, String sysPeriod) {
    this.dbHistorySupport = dbHistorySupport;
    this.asOfTableMap = asOfTableMap;
    this.sysPeriod = sysPeriod;
  }

  public String getAsOfView(String table) {
    return asOfTableMap.get(table);
  }

  public String getSysPeriodLower(String tableAlias) {

    return dbHistorySupport.getSysPeriodLower(tableAlias, sysPeriod);
  }

  public String getSysPeriodUpper(String tableAlias) {

    return dbHistorySupport.getSysPeriodUpper(tableAlias, sysPeriod);
  }

  public String getAsOfPredicate(String tableAlias) {

    return dbHistorySupport.getAsOfPredicate(tableAlias, sysPeriod);
  }
}
