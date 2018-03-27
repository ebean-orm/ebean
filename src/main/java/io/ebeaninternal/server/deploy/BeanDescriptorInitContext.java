package io.ebeaninternal.server.deploy;

import java.util.Map;

class BeanDescriptorInitContext {

  private final Map<String, String> withHistoryTables;
  private final Map<String, String> draftTables;
  private final String asOfViewSuffix;

  BeanDescriptorInitContext(Map<String, String> withHistoryTables, Map<String, String> draftTables, String asOfViewSuffix) {
    this.withHistoryTables = withHistoryTables;
    this.draftTables = draftTables;
    this.asOfViewSuffix = asOfViewSuffix;
  }

  void addDraft(String baseTable, String draftTable) {
    draftTables.put(baseTable, draftTable);
  }

  void addHistory(String baseTable, String baseTableAsOf) {
    withHistoryTables.put(baseTable, baseTableAsOf);
  }

  void addHistoryIntersection(String intersectionTableName) {
    withHistoryTables.put(intersectionTableName, intersectionTableName + asOfViewSuffix);
  }

  void addDraftIntersection(String intersectionPublishTable, String intersectionDraftTable) {
    draftTables.put(intersectionPublishTable, intersectionDraftTable);
  }
}
