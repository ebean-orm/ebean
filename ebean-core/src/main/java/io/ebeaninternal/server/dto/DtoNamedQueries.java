package io.ebeaninternal.server.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of named queries for a single Dto bean type.
 */
public class DtoNamedQueries {

  private Map<String, String> namedRawSql = new HashMap<>();

  /**
   * Add the named query from deployment XML.
   */
  public void addRawSql(String name, String query) {
    namedRawSql.put(name, query);
  }

  Map<String, String> map() {
    return namedRawSql;
  }
}
