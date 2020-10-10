package io.ebeaninternal.xmapping.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmapRawSql {

  protected final String name;
  protected final String query;
  protected final Map<String, String> columnMapping = new LinkedHashMap<>();
  protected final Map<String, String> aliasMapping = new LinkedHashMap<>();

  public XmapRawSql(String name, String query) {
    this.name = name;
    this.query = query;
  }

  public void addColumnMapping(String column, String property) {
    columnMapping.put(column, property);
  }

  public void addAliasMapping(String alias, String property) {
    aliasMapping.put(alias, property);
  }

  public String getName() {
    return name;
  }

  public String getQuery() {
    return query;
  }

  public Map<String, String> getColumnMapping() {
    return columnMapping;
  }

  public Map<String, String> getAliasMapping() {
    return aliasMapping;
  }

}
