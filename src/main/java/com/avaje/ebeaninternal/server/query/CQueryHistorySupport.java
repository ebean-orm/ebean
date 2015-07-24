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
  private final Map<String,String> asOfTableMap;

  /**
   * The sys period column.
   */
  private final String asOfSysPeriod;

  public CQueryHistorySupport(DbHistorySupport dbHistorySupport, Map<String, String> asOfTableMap, String asOfSysPeriod) {
    this.dbHistorySupport = dbHistorySupport;
    this.asOfTableMap = asOfTableMap;
    this.asOfSysPeriod = asOfSysPeriod;
  }

  public String getAsOfView(String table) {
    return asOfTableMap.get(table);
  }

  public String getSysPeriodLower(String tableAlias) {

    return dbHistorySupport.getSysPeriodLower(tableAlias, asOfSysPeriod);
  }

  public String getSysPeriodUpper(String tableAlias) {

    return dbHistorySupport.getSysPeriodUpper(tableAlias, asOfSysPeriod);
  }

}
