package io.ebeaninternal.server.query;

import io.ebean.config.dbplatform.DbHistorySupport;

import java.util.Map;

/**
 * Helper to support history functions.
 */
class CQueryHistorySupport {

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

  CQueryHistorySupport(DbHistorySupport dbHistorySupport, Map<String, String> asOfTableMap, String sysPeriod) {
    this.dbHistorySupport = dbHistorySupport;
    this.asOfTableMap = asOfTableMap;
    this.sysPeriod = sysPeriod;
  }

  /**
   * Return true if the underlying history support is standards based.
   */
  boolean isStandardsBased() {
    return dbHistorySupport.isStandardsBased();
  }

  /**
   * Return the 'as of' history view for the given base table.
   */
  String getAsOfView(String table) {
    return asOfTableMap.get(table);
  }

  /**
   * Return the lower bound column.
   */
  String getSysPeriodLower(String tableAlias) {
    return dbHistorySupport.getSysPeriodLower(tableAlias, sysPeriod);
  }

  /**
   * Return the upper bound column.
   */
  String getSysPeriodUpper(String tableAlias) {
    return dbHistorySupport.getSysPeriodUpper(tableAlias, sysPeriod);
  }

  /**
   * Return the predicate appended to the end of the query.
   * <p>
   * Note used for Oracle total recall etc with the more standard approach.
   */
  String getAsOfPredicate(String tableAlias) {
    return dbHistorySupport.getAsOfPredicate(tableAlias, sysPeriod);
  }

}
