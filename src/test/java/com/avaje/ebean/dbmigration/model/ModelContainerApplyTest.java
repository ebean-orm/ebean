package com.avaje.ebean.dbmigration.model;

import com.avaje.ebean.dbmigration.migration.AddColumn;
import com.avaje.ebean.dbmigration.migration.ChangeSet;
import com.avaje.ebean.dbmigration.migration.CreateTable;
import com.avaje.ebean.dbmigration.migration.DropColumn;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlReader;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelContainerApplyTest {

  @Test
  public void testApply() throws Exception {

    Migration migration = MigrationXmlReader.read("/container/test-create-table.xml");

    List<ChangeSet> changeSets = migration.getChangeSet();
    ChangeSet changeSet = changeSets.get(0);

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    assertThat(changeSetChildren).hasSize(3);
    assertThat(changeSetChildren.get(0)).isInstanceOf(CreateTable.class);
    assertThat(changeSetChildren.get(1)).isInstanceOf(AddColumn.class);
    assertThat(changeSetChildren.get(2)).isInstanceOf(DropColumn.class);

    ModelContainer model = new ModelContainer();
    model.apply(migration);

    MTable foo = model.getTable("foo");
    assertThat(foo.getComment()).isEqualTo("comment");
    assertThat(foo.getTablespace()).isEqualTo("fooSpace");
    assertThat(foo.getIndexTablespace()).isEqualTo("fooIndexSpace");
    assertThat(foo.isWithHistory()).isEqualTo(false);
    assertThat(foo.getColumns()).containsKeys("col1", "col3", "added_to_foo");
  }
}