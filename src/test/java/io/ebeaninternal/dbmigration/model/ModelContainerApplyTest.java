package io.ebeaninternal.dbmigration.model;

import io.ebeaninternal.dbmigration.migration.AddColumn;
import io.ebeaninternal.dbmigration.migration.ChangeSet;
import io.ebeaninternal.dbmigration.migration.CreateTable;
import io.ebeaninternal.dbmigration.migration.DropColumn;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlReader;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelContainerApplyTest {

  @Test
  public void testApply() {

    Migration migration = MigrationXmlReader.read("/container/test-create-table.xml");

    List<ChangeSet> changeSets = migration.getChangeSet();
    ChangeSet changeSet = changeSets.get(0);

    List<Object> changeSetChildren = changeSet.getChangeSetChildren();
    assertThat(changeSetChildren).hasSize(3);
    assertThat(changeSetChildren.get(0)).isInstanceOf(CreateTable.class);
    assertThat(changeSetChildren.get(1)).isInstanceOf(AddColumn.class);
    assertThat(changeSetChildren.get(2)).isInstanceOf(DropColumn.class);

    ModelContainer model = new ModelContainer();
    model.apply(migration, MigrationVersion.parse("1.1"));

    MTable foo = model.getTable("foo");
    assertThat(foo.getComment()).isEqualTo("comment");
    assertThat(foo.getTablespace()).isEqualTo("fooSpace");
    assertThat(foo.getIndexTablespace()).isEqualTo("fooIndexSpace");
    assertThat(foo.isWithHistory()).isEqualTo(false);
    assertThat(foo.allColumns()).extracting("name").contains("col1", "col3", "added_to_foo");
  }
}
