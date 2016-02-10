package com.avaje.ebean.dbmigration.ddlgeneration.platform;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class HistoryTableUpdateTest {

  @Test
  public void testToRevertedColumns_add() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    assertThat(upd.getBaseTable()).isEqualTo("mytab");

    upd.add(HistoryTableUpdate.Change.ADD, "two");
    assertThat(upd.hasApplyChanges()).isTrue();
    assertThat(upd.hasDropChanges()).isFalse();

    List<String> current = current();
    upd.toRevertedColumns(current);
    assertThat(current).contains("one","three");
  }

  @Test
  public void testToRevertedColumns_include() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.INCLUDE, "two");
    assertThat(upd.hasApplyChanges()).isTrue();
    assertThat(upd.hasDropChanges()).isFalse();

    List<String> current = current();
    upd.toRevertedColumns(current);
    assertThat(current).contains("one","three");
  }

  @Test
  public void testToRevertedColumns_drop() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.DROP, "three");
    assertThat(upd.hasApplyChanges()).isFalse();
    assertThat(upd.hasDropChanges()).isTrue();

    List<String> current = current();
    upd.toRevertedColumns(current);
    assertThat(current).contains("one","two","three");
  }

  @Test
  public void testToRevertedColumns_exclude() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.EXCLUDE, "four");
    assertThat(upd.hasApplyChanges()).isTrue();
    assertThat(upd.hasDropChanges()).isFalse();

    List<String> current = current();
    upd.toRevertedColumns(current);
    assertThat(current).contains("one","two","three","four");
  }


  @Test
  public void testDescription() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.ADD, "two");
    upd.add(HistoryTableUpdate.Change.DROP, "four");
    assertThat(upd.hasApplyChanges()).isTrue();
    assertThat(upd.hasDropChanges()).isTrue();

    assertThat(upd.descriptionForApply()).isEqualTo("add two");
    assertThat(upd.descriptionForDrop()).isEqualTo("drop four");
  }

  @Test
  public void testDescription_withIncludeExclude() throws Exception {

    HistoryTableUpdate upd = new HistoryTableUpdate("mytab");
    upd.add(HistoryTableUpdate.Change.ADD, "two");
    upd.add(HistoryTableUpdate.Change.INCLUDE, "five");
    upd.add(HistoryTableUpdate.Change.EXCLUDE, "six");
    upd.add(HistoryTableUpdate.Change.DROP, "four");
    assertThat(upd.hasApplyChanges()).isTrue();
    assertThat(upd.hasDropChanges()).isTrue();

    assertThat(upd.descriptionForApply()).isEqualTo("add two, include five, exclude six");
    assertThat(upd.descriptionForDrop()).isEqualTo("drop four");
  }

  List<String> current() {

    List<String> currentColumns = new ArrayList<String>();
    currentColumns.add("one");
    currentColumns.add("two");
    currentColumns.add("three");
    return currentColumns;
  }
}