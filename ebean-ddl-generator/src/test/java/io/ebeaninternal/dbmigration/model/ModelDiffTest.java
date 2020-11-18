package io.ebeaninternal.dbmigration.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ModelDiffTest {

  @Test
  public void test_compareTo_with_dropColumnOnHistoryTable_then_historyColumnRegistered() throws Exception {

    ModelContainer base = new ModelContainer();
    base.addTable(MTableTest.base().setWithHistory(true));

    ModelContainer newModel = new ModelContainer();
    newModel.addTable(MTableTest.newTable().setWithHistory(true));

    ModelDiff diff = new ModelDiff(base);
    diff.compareTo(newModel);

    MTable tab = newModel.getTable("tab");

    assertThat(tab.allHistoryColumns(true)).contains("status");
    assertThat(tab.allColumns()).extracting("name").doesNotContain("status");
  }
}
