package io.ebeaninternal.server.deploy;

import java.util.Map;

class BeanDescriptorInitContext {

  private final Map<String, String> withHistoryTables;
  private final String asOfViewSuffix;
  private String embeddedPrefix;

  BeanDescriptorInitContext(Map<String, String> withHistoryTables, String asOfViewSuffix) {
    this.withHistoryTables = withHistoryTables;
    this.asOfViewSuffix = asOfViewSuffix;
  }

  void addHistory(String baseTable, String baseTableAsOf) {
    withHistoryTables.put(baseTable, baseTableAsOf);
  }

  void addHistoryIntersection(String intersectionTableName) {
    withHistoryTables.put(intersectionTableName, intersectionTableName + asOfViewSuffix);
  }

  public void setEmbeddedPrefix(String embeddedPrefix) {
    this.embeddedPrefix = embeddedPrefix;
  }

  public String getEmbeddedPrefix() {
    return embeddedPrefix;
  }
}
