package io.ebeaninternal.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class HistoryTableUpdateTest {

  @Test
  public void testDescription() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.ADD, "two");
    upd.add(HistoryTableUpdate.Change.DROP, "four");

    assertThat(upd.description()).isEqualTo("[add two, drop four]");
  }

  @Test
  public void testDescription_withIncludeExclude() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.ADD, "two");
    upd.add(HistoryTableUpdate.Change.INCLUDE, "five");
    upd.add(HistoryTableUpdate.Change.EXCLUDE, "six");
    upd.add(HistoryTableUpdate.Change.DROP, "four");

    assertThat(upd.description()).isEqualTo("[add two, include five, exclude six, drop four]");
  }

}
