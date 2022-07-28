package io.ebeaninternal.dbmigration.model;

import io.ebean.migration.MigrationVersion;
import io.ebeaninternal.dbmigration.migration.*;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelContainerApplyTest {

  @Test
  void testApply() {
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
    assertThat(foo.getTablespaceMeta().getTablespaceName()).isEqualTo("db2;fooSpace;");
    assertThat(foo.getTablespaceMeta().getIndexTablespace()).isEqualTo("db2;fooIndexSpace;");
    assertThat(foo.getTablespaceMeta().getLobTablespace()).isEqualTo("db2;fooLobSpace;");
    assertThat(foo.isWithHistory()).isEqualTo(false);
    assertThat(foo.allColumns()).extracting("name").contains("col1", "col3", "added_to_foo");
  }

  @Test
  void createSchema() {
    CreateSchema createSchema = new CreateSchema();
    createSchema.setName("foo");

    Migration migration = newMigration(createSchema);

    ModelContainer model = new ModelContainer();
    model.apply(migration, MigrationVersion.parse("1.1"));
  }

  private Migration newMigration(Object change) {
    ChangeSet changeSet = new ChangeSet();
    changeSet.getChangeSetChildren().add(change);
    Migration migration = new Migration();
    migration.getChangeSet().add(changeSet);
    return migration;
  }
}
