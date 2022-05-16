package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.CreateSchema;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelDiffTest {

  @Test
  void test_compareTo_with_dropColumnOnHistoryTable_then_historyColumnRegistered() throws Exception {
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

  @Test
  void schemaDiff() {
    ModelContainer base = new ModelContainer();
    base.addTable(new MTable("foo.one"));
    base.addTable(new MTable("bar.two"));

    ModelDiff modelDiff = new ModelDiff(base);

    ModelContainer newOne = new ModelContainer();
    newOne.addTable(new MTable("bazz.three"));
    newOne.addTable(new MTable("foo.one"));
    newOne.addTable(new MTable("buzz.four"));

    modelDiff.compareTo(newOne);
    ChangeSet apply = modelDiff.getApplyChangeSet();
    List<Object> children = apply.getChangeSetChildren();
    assertThat(children).hasSize(4);
    assertThat(((CreateSchema) children.get(0)).getName()).isEqualTo("bazz");
    assertThat(((CreateSchema) children.get(1)).getName()).isEqualTo("buzz");
    assertThat(children.get(2)).isInstanceOf(CreateTable.class);
    assertThat(children.get(3)).isInstanceOf(CreateTable.class);
  }
}
