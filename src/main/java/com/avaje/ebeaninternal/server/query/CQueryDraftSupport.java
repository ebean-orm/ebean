package com.avaje.ebeaninternal.server.query;

import java.util.Map;

/**
 * Support 'asDraft' queries.
 */
public class CQueryDraftSupport {

  /**
   * The mapping of base tables to their matching 'draft' table.
   */
  private final Map<String, String> tableMap;

  public CQueryDraftSupport(Map<String, String> tableMap) {
    this.tableMap = tableMap;
  }

  /**
   * Return the draft table associated to the base table.
   *
   * This returns null for entities that are not draftable and in that case
   * the usual base table is used.
   */
  public String getDraftTable(String table) {
    return tableMap.get(table);
  }
}
